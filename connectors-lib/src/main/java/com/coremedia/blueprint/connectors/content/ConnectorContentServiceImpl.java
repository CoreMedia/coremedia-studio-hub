package com.coremedia.blueprint.connectors.content;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContentService;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.impl.ConnectorContextImpl;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.rest.cap.content.search.SearchServiceResult;
import com.coremedia.rest.cap.content.search.solr.SolrSearchService;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.CONNECTOR_ID;

/**
 * The service implemented the asynchronous content creation for connector items.
 * Since downloading a entity of an external system may take some type, the content
 * creation is implemented on the client side, while the post processing is triggered
 * as a separate thread on the server side. As long as this service is processing the content,
 * it is locked by the webserver.
 */
public class ConnectorContentServiceImpl implements ConnectorContentService, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorContentServiceImpl.class);
  private static final String LOCAL_SETTINGS = "localSettings";

  private ExecutorService service = Executors.newCachedThreadPool();

  private ContentRepository contentRepository;
  private Connectors connectors;
  private SolrSearchService solrSearchService;
  private int timeoutSeconds = 180;

  @Autowired
  private List<ContentWriteInterceptor> contentWriteInterceptors;
  private ConnectorContextProvider connectorContextProvider;

  @Override
  public Content createContent(@Nonnull ConnectorId connectorId, @Nonnull String folder, Site site) {
    ConnectorContext context = connectorContextProvider.createContext(connectorId.getConnectionId());
    ConnectorConnection connection = connectors.getConnection(context);
    if (connection != null) {
      ConnectorItem item = connection.getConnectorService().getItem(connectorId);
      if (findContent(connectorId, site) != null) {
        LOG.info("Ignored duplicate content creation for connector item '" + item.getDisplayName() + ", type " + item.getItemType());
        return null;
      }

      Content targetFolder = contentRepository.getChild(folder);
      String targetContentTypeName = context.getContentMappings().get(item.getItemType());
      ContentType contentType = contentRepository.getContentType(targetContentTypeName);
      String name = item.getDisplayName();

      Content newContent = contentType.createByTemplate(targetFolder, name, "{3} ({1})", new HashMap<>());
      setConnectorId(newContent, item.getConnectorId());
      return newContent;
    }

    return null;
  }

  @Override
  public Content findContent(@Nonnull ConnectorId connectorId, Site site) {
    ConnectorContext context = connectorContextProvider.createContext(connectorId.getConnectionId());
    ConnectorConnection connection = connectors.getConnection(context);
    ConnectorItem item = connection.getConnectorService().getItem(connectorId);

    //when no value is specified, we allow duplicate content creation
    String contentScope = context.getContentScope();
    if (contentScope == null) {
      return null;
    }

    String targetContentType = context.getContentMappings().get(item.getItemType());
    Content root = contentRepository.getRoot();
    if (site != null) {
      if (contentScope.equals(ConnectorContextImpl.CONTENT_SCOPE_SITE)) {
        root = site.getSiteRootFolder();
      }
      else if (contentScope.equals(ConnectorContextImpl.CONTENT_SCOPE_DOMAIN)) {
        root = site.getSiteRootFolder().getParent();
      }
    }

    String idString = connectorId.toString();
    SearchServiceResult result = solrSearchService.search("", -1,
            new ArrayList<>(),
            root,
            true,
            Arrays.asList(contentRepository.getContentType(targetContentType)),
            false,
            Arrays.asList("textbody:\"" + idString + "\"", "isdeleted:false"),
            new ArrayList<>(),
            new ArrayList<>());
    if (!result.getHits().isEmpty()) {
      return result.getHits().get(0);
    }
    return null;
  }

  @Override
  public void processContent(@Nonnull Content content,
                             @Nonnull ConnectorItem item,
                             boolean wait) {
    try {
      ConnectorContext context = connectorContextProvider.createContext(item.getConnectorId().getConnectionId());
      ConnectorContentCallable callable = new ConnectorContentCallable(context, content, item, contentWriteInterceptors, solrSearchService);
      Future<Void> submit = service.submit(callable);
      if (wait) {
        submit.get(timeoutSeconds, TimeUnit.SECONDS);
      }
    } catch (Exception e) {
      LOG.error("Error submitting connector content callable for " + item + ": " + e.getMessage(), e);
    }
  }

  @Override
  public void setConnectorId(@Nonnull Content content, @Nonnull ConnectorId id) {
    CapPropertyDescriptor descriptor = content.getType().getDescriptor(LOCAL_SETTINGS);
    if (descriptor != null && descriptor.getType().equals(CapPropertyDescriptorType.STRUCT)) {
      Struct struct = content.getStruct(LOCAL_SETTINGS);
      if (struct == null) {
        StructService structService = content.getRepository().getConnection().getStructService();
        struct = structService.emptyStruct();
      }

      StructBuilder builder = struct.builder();
      builder.set(CONNECTOR_ID, id.toString());
      Struct updatedStruct = builder.build();
      if (!content.isCheckedOut()) {
        content.checkOut();
      }
      content.set(LOCAL_SETTINGS, updatedStruct);
      content.checkIn();
      content.getRepository().getConnection().flush();
    }
  }


  //------------------- Spring -----------------------------------------------------------------------------------------
  @Override
  public void afterPropertiesSet() {
    //ensure the priority order
    contentWriteInterceptors.sort(Comparator.comparingInt(ContentWriteInterceptor::getPriority));
  }

  @Required
  public void setConnectors(Connectors connectors) {
    this.connectors = connectors;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setConnectorContextProvider(ConnectorContextProvider connectorContextProvider) {
    this.connectorContextProvider = connectorContextProvider;
  }

  public void setTimeoutSeconds(int timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }

  @Required
  public void setSolrSearchService(SolrSearchService solrSearchService) {
    this.solrSearchService = solrSearchService;
  }
}
