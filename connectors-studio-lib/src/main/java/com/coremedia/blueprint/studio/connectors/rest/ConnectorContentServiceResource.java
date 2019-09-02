package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.studio.connectors.rest.content.ConnectorContentService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.FormParam;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "connector/contentservice", produces = APPLICATION_JSON_VALUE)
public class ConnectorContentServiceResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorContentServiceResource.class);

  private static final String ID_PARAM = "id";
  private static final String FOLDER_PARAM = "folder";
  private static final String CONTENT_ID_PARAM = "contentId";
  private static final String SITE_ID_PARAM = "siteId";

  private Connectors connector;
  private ConnectorContextProvider connectorContextProvider;
  private SitesService sitesService;
  private ContentRepository contentRepository;
  private ConnectorContentService connectorContentService;


  public ConnectorContentServiceResource(Connectors connector, ConnectorContextProvider connectorContextProvider, SitesService sitesService, ContentRepository contentRepository, ConnectorContentService connectorContentService) {
    this.connector = connector;
    this.connectorContextProvider = connectorContextProvider;
    this.sitesService = sitesService;
    this.contentRepository = contentRepository;
    this.connectorContentService = connectorContentService;
  }

  @PostMapping("content/{" + SITE_ID_PARAM + "}")
  public Content findContent(@PathVariable(SITE_ID_PARAM) String siteId,
                             @RequestParam(FOLDER_PARAM) String folder,
                             @RequestParam(ID_PARAM) String entityId) {
    try {
      ConnectorId connectorId = ConnectorId.toId(entityId);
      ConnectorConnection connection = getConnection(connectorId.getConnectionId());
      ConnectorContext context = connection.getContext();
      Site site = sitesService.getSite(siteId);

      ConnectorEntity entity = null;
      if (connectorId.isItemId()) {
        entity = connection.getConnectorService().getItem(context, connectorId);
      } else {
        entity = connection.getConnectorService().getCategory(context, connectorId);
      }

      return connectorContentService.findContent(entity, folder, site);
    } catch (Exception e) {
      LOGGER.error("Failed to lookup content for connector item " + entityId + ": " + e.getMessage(), e);
    }
    return null;
  }

  @PostMapping("create/{" + SITE_ID_PARAM + "}")
  public Content create(@PathVariable(SITE_ID_PARAM) String siteId,
                        @RequestParam(FOLDER_PARAM) String folder,
                        @RequestParam(ID_PARAM) String entityId) {
    try {
      ConnectorId connectorId = ConnectorId.toId(entityId);
      return connectorContentService.createContent(connectorId, folder);
    } catch (Exception e) {
      LOGGER.error("Failed to create content for connector item " + entityId + ": " + e.getMessage(), e);
    }
    return null;
  }

  @PostMapping("process/{" + SITE_ID_PARAM + "}")
  public String process(@RequestParam(ID_PARAM) String entityId,
                        @RequestParam(CONTENT_ID_PARAM) String contentId) {
    try {
      ConnectorId connectorId = ConnectorId.toId(entityId);
      ConnectorConnection connection = getConnection(connectorId.getConnectionId());
      ConnectorService connectorService = connection.getConnectorService();
      ConnectorContext context = connection.getContext();
      ConnectorEntity entity = null;
      if (connectorId.isItemId()) {
        entity = connectorService.getItem(context, connectorId);
      } else {
        entity = connectorService.getCategory(context, connectorId);
      }

      if (entity != null) {
        Content content = contentRepository.getContent(contentId);
        //note that this method is only called when the quick create was uses,
        //therefore we have to set the connector id which has not been set by the dialog
        connectorContentService.setConnectorId(content, connectorId);
        connectorContentService.processContent(content, entity, false);
      }
      return null;
    } catch (Exception e) {
      LOGGER.error("Failed to post process connector item " + entityId + ": " + e.getMessage(), e);
      return "Error in connector item processing: " + e.getMessage();
    }
  }

  //---------------------- Helper --------------------------------------------------------------------------------------

  private ConnectorConnection getConnection(String connectorConnectionId) {
    ConnectorContext connectorContext = getConnectorContext(connectorConnectionId);
    return connector.getConnection(connectorContext);
  }

  @NonNull
  private ConnectorContext getConnectorContext(String connectionId) {
    return connectorContextProvider.createContext(connectionId);
  }

  //---------------------- Spring --------------------------------------------------------------------------------------

  public void setConnector(Connectors connector) {
    this.connector = connector;
  }

  public void setConnectorContextProvider(ConnectorContextProvider connectorContextProvider) {
    this.connectorContextProvider = connectorContextProvider;
  }

  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public void setConnectorContentService(ConnectorContentService connectorContentService) {
    this.connectorContentService = connectorContentService;
  }
}
