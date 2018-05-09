package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContentService;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("connector/contentservice")
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

  @POST
  @Path("content/{siteId:[^/]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public Content findContent(@PathParam(SITE_ID_PARAM) String siteId,
                             @FormParam(FOLDER_PARAM) String folder,
                             @FormParam(ID_PARAM) String entityId) {
    try {
      ConnectorId connectorId = ConnectorId.toId(entityId);
      ConnectorConnection connection = getConnection(connectorId.getConnectionId());
      ConnectorContentService connectorContentService = connection.getConnectorContentService();
      ConnectorContext context = connection.getContext();
      Site site = sitesService.getSite(siteId);

      ConnectorEntity entity = null;
      if(connectorId.isItemId()) {
        entity = connection.getConnectorService().getItem(context, connectorId);
      }
      else {
        entity = connection.getConnectorService().getCategory(context, connectorId);
      }

      return connectorContentService.findContent(entity, folder, site);
    } catch (Exception e) {
      LOGGER.error("Failed to lookup content for connector item " + entityId + ": " + e.getMessage(), e);
    }
    return null;
  }

  @POST
  @Path("create/{siteId:[^/]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public Content create(@PathParam(SITE_ID_PARAM) String siteId,
                        @FormParam(FOLDER_PARAM) String folder,
                        @FormParam(ID_PARAM) String entityId) {
    try {
      ConnectorId connectorId = ConnectorId.toId(entityId);
      ConnectorConnection connection = getConnection(connectorId.getConnectionId());
      ConnectorContentService connectorContentService = connection.getConnectorContentService();
      Site site = sitesService.getSite(siteId);
      return connectorContentService.createContent(connectorId, folder, site);
    } catch (Exception e) {
      LOGGER.error("Failed to create content for connector item " + entityId + ": " + e.getMessage(), e);
    }
    return null;
  }

  @POST
  @Path("process/{siteId:[^/]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public String process(@FormParam(ID_PARAM) String entityId,
                        @FormParam(CONTENT_ID_PARAM) String contentId) {
    try {
      ConnectorId connectorId = ConnectorId.toId(entityId);
      ConnectorConnection connection = getConnection(connectorId.getConnectionId());
      ConnectorService connectorService = connection.getConnectorService();
      ConnectorContext context = connection.getContext();
      ConnectorEntity entity = null;
      if(connectorId.isItemId()) {
        entity = connectorService.getItem(context, connectorId);
      }
      else {
        entity = connectorService.getCategory(context, connectorId);
      }

      if (entity != null) {
        Content content = contentRepository.getContent(contentId);
        //note that this method is only called when the quick create was uses,
        //therefore we have to set the connector id which has not been set by the dialog
        connection.getConnectorContentService().setConnectorId(content, connectorId);
        connection.getConnectorContentService().processContent(content, entity, false);
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

  @Nonnull
  private ConnectorContext getConnectorContext(String connectionId) {
    return connectorContextProvider.createContext(connectionId);
  }

  //---------------------- Spring --------------------------------------------------------------------------------------

  @Required
  public void setConnector(Connectors connector) {
    this.connector = connector;
  }

  @Required
  public void setConnectorContextProvider(ConnectorContextProvider connectorContextProvider) {
    this.connectorContextProvider = connectorContextProvider;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }
}
