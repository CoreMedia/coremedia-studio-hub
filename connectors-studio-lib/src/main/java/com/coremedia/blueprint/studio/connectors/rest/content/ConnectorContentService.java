package com.coremedia.blueprint.studio.connectors.rest.content;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.impl.ConnectorContextImpl;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.common.CapSession;
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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
public class ConnectorContentService implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(ConnectorContentService.class);

  public static final String LOCAL_SETTINGS = "localSettings";

  private ExecutorService service = Executors.newCachedThreadPool();

  private ContentRepository contentRepository;
  private Connectors connectors;
  private SolrSearchService solrSearchService;
  private int timeoutSeconds = 180;

  @Autowired
  private List<ContentWriteInterceptor> contentWriteInterceptors;
  private ConnectorContextProvider connectorContextProvider;

  @Value("${repository.user}")
  private String repositoryUser;

  @Value("${repository.password}")
  private String repositoryPassword;

  public ConnectorContentService(Connectors connectors, ConnectorContextProvider connectorContextProvider, ContentRepository contentRepository, SolrSearchService solrSearchService) {
    this.contentRepository = contentRepository;
    this.connectors = connectors;
    this.solrSearchService = solrSearchService;
    this.connectorContextProvider = connectorContextProvider;
  }

  @Nullable
  public Content createContent(@NonNull ConnectorId connectorId, @NonNull String folder) {
    ConnectorContext context = connectorContextProvider.createContext(connectorId.getConnectionId());
    ConnectorConnection connection = connectors.getConnection(context);
    if (connection == null) {
      return null;
    }
    Content targetFolder = contentRepository.getChild(folder);

    if (!connectorId.isItemId()) {
      ConnectorCategory category = connection.getConnectorService().getCategory(context, connectorId);
      return contentRepository.createSubfolders(targetFolder, category.getName());
    }

    ConnectorItem item = connection.getConnectorService().getItem(context, connectorId);
    String targetContentTypeName = item.getTargetContentType();
    ContentType contentType = contentRepository.getContentType(targetContentTypeName);
    String name = item.getDisplayName();
    //remove unwanted characters
    if (StringUtils.isNotBlank(name)) {
      name = name.replaceAll("[^\\x00-\\x7F]", "").trim();
    }

    Content newContent = contentType.createByTemplate(targetFolder, name, "{3} ({1})", new HashMap<>());
    setConnectorId(newContent, item.getConnectorId());
    return newContent;
  }

  @Nullable
  public Content findContent(@NonNull ConnectorEntity entity, @Nullable String folder, @Nullable Site site) {
    ConnectorId connectorId = entity.getConnectorId();
    if (connectorId.isItemId()) {
      return findContentItem(connectorId, entity, site);
    }

    return findContentCategory(entity, folder);
  }

  public void processContent(@NonNull Content content,
                             @NonNull ConnectorEntity entity,
                             boolean wait) {
    try {
      ConnectorContext context = connectorContextProvider.createContext(entity.getConnectorId().getConnectionId());

      CapSession session = contentRepository.getConnection().getSession();
      ConnectorContentServiceCallable callable = new ConnectorContentServiceCallable(session, context, content, entity, contentWriteInterceptors, solrSearchService);
      Future<Void> submit = service.submit(callable);
      if (wait) {
        submit.get(timeoutSeconds, TimeUnit.SECONDS);
      }
    } catch (Exception e) {
      LOG.error("Error submitting connector content callable for " + entity + ": " + e.getMessage(), e);
    }
  }

  public void setConnectorId(@NonNull Content content, @NonNull ConnectorId id) {
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

  //------------------- Helper -----------------------------------------------------------------------------------------

  @Nullable
  private Content findContentCategory(@NonNull ConnectorEntity entity, @Nullable String folder) {
    Content parentFolder = contentRepository.getChild(folder);
    Set<Content> children = parentFolder.getChildren();
    for (Content child : children) {
      if(child.getName().equals(entity.getName())) {
        return child;
      }
    }
    return null;
  }

  @Nullable
  private Content findContentItem(@NonNull ConnectorId connectorId, @NonNull ConnectorEntity entity, @Nullable Site site) {
    ConnectorContext context = connectorContextProvider.createContext(entity.getConnectorId().getConnectionId());
    ConnectorConnection connection = connectors.getConnection(context);
    String contentScope = context.getContentScope();

    Content root = contentRepository.getRoot();
    if (site != null && contentScope != null) {
      if (contentScope.equals(ConnectorContextImpl.CONTENT_SCOPE_SITE)) {
        root = site.getSiteRootFolder();
      }
      else if (contentScope.equals(ConnectorContextImpl.CONTENT_SCOPE_DOMAIN)) {
        root = site.getSiteRootFolder().getParent();
      }
    }

    ConnectorItem item = connection.getConnectorService().getItem(context, connectorId);
    String targetContentType = item.getTargetContentType();
    String idString = connectorId.toString();
    List<String> filterQueries = Arrays.asList("textbody:\"" + idString + "\"", "isdeleted:false");

    SearchServiceResult result = solrSearchService.search("", -1,
            new ArrayList<>(),
            root,
            true,
            Arrays.asList(contentRepository.getContentType(targetContentType)),
            false,
            filterQueries,
            new ArrayList<>(),
            new ArrayList<>());
    if (!result.getHits().isEmpty()) {
      return result.getHits().get(0);
    }

    return  null;
  }

}
