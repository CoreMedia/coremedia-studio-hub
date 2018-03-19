package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorSearchResultRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorSuggestionRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorSuggestionResultRepresentation;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.multisite.SitesService;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Path("connector/service")
public class ConnectorServiceResource {
  private static final String SEARCH_PARAM_QUERY = "query";
  private static final String SEARCH_PARAM_LIMIT = "limit";
  private static final String SEARCH_PARAM_ORDER_BY = "orderBy";
  private static final String SEARCH_PARAM_SEARCH_TYPE = "searchType";
  private static final String SEARCH_PARAM_CATEGORY = "category";

  private static final String SITE_ID_PARAM = "siteId";
  private static final String CONNECTOR_CONNECTION_ID = "connectorConnectionId";
  private static final String CONNECTOR_TYPE = "connectorType";

  private Connectors connector;
  private ConnectorContextProvider connectorContextProvider;
  private SitesService sitesService;

  @GET
  @Path("search/{connectorType}/{connectorConnectionId:[^/]+}/{siteId:[^/]+}")
  @Nullable
  public ConnectorSearchResultRepresentation search(@PathParam(CONNECTOR_TYPE) String connectorType,
                                                    @PathParam(CONNECTOR_CONNECTION_ID) String connectionId,
                                                    @PathParam(SITE_ID_PARAM) String siteId,
                                                    @QueryParam(SEARCH_PARAM_QUERY) String query,
                                                    @QueryParam(SEARCH_PARAM_LIMIT) int limit,
                                                    @QueryParam(SEARCH_PARAM_ORDER_BY) String orderBy,
                                                    @QueryParam(SEARCH_PARAM_CATEGORY) String categoryId,
                                                    @QueryParam(SEARCH_PARAM_SEARCH_TYPE) String searchType) {
    ConnectorId id = null;
    if (categoryId != null) {
      id = ConnectorId.toId(categoryId);
    }

    ConnectorSearchResult<ConnectorEntity> searchResult = search(query, connectorType, connectionId, siteId, id, searchType);
    return new ConnectorSearchResultRepresentation(searchResult.getSearchResult(), searchResult.getTotalCount());
  }

  @GET
  @Path("suggestions/{connectorType}/{connectorConnectionId:[^/]+}/{siteId:[^/]+}")
  @Nonnull
  public ConnectorSuggestionResultRepresentation searchSuggestions(@PathParam(CONNECTOR_TYPE) String connectorType,
                                                                   @PathParam(CONNECTOR_CONNECTION_ID) String connectionId,
                                                                   @PathParam(SITE_ID_PARAM) String siteId,
                                                                   @QueryParam(SEARCH_PARAM_QUERY) String query,
                                                                   @QueryParam(SEARCH_PARAM_LIMIT) int limit,
                                                                   @QueryParam(SEARCH_PARAM_SEARCH_TYPE) String searchType,
                                                                   @QueryParam(SEARCH_PARAM_CATEGORY) String categoryId) {
    ConnectorId connectorId = null;
    if (categoryId != null) {
      connectorId = ConnectorId.toId(categoryId);
    }
    ConnectorSearchResult<ConnectorEntity> searchResult = search(query, connectorType, connectionId, siteId, connectorId, searchType);
    List<ConnectorEntity> result = searchResult.getSearchResult();
    List<ConnectorSuggestionRepresentation> suggestions = new ArrayList<>();
    for (ConnectorEntity connectorObject : result) {
      suggestions.add(new ConnectorSuggestionRepresentation(connectorObject.getDisplayName(), 1));
    }

    return new ConnectorSuggestionResultRepresentation(suggestions);
  }

  //---------------------- Helper --------------------------------------------------------------------------------------

  private ConnectorSearchResult<ConnectorEntity> search(String query,
                                                        String connectorType,
                                                        String connectionId,
                                                        String siteId,
                                                        ConnectorId categoryId,
                                                        String searchType) {
    if (ContentType.DOCUMENT.equals(searchType)) {
      searchType = null; //search all
    }

    List<ConnectorConnection> connections = new ArrayList<>();
    if (connectionId == null || connectionId.equals("all")) {
      List<ConnectorContext> contexts = connectorContextProvider.findContexts(sitesService.getSite(siteId));
      for (ConnectorContext context : contexts) {
        ConnectorConnection connection = connector.getConnection(context);
        if (connection != null) {
          connections.add(connection);
        }
      }
    }
    else {
      ConnectorContext context = connectorContextProvider.createContext(connectionId);
      if (context != null) {
        ConnectorConnection connection = connector.getConnection(context);
        if (connection != null) {
          connections.add(connection);
        }
      }
    }

    //collect search results from multiple connection, possible when a system, not a category is selected
    ConnectorSearchResult<ConnectorEntity> searchResult = new ConnectorSearchResult<>(new ArrayList<>());
    for (ConnectorConnection connection : connections) {
      //filter search for types
      String connectionType = connection.getContext().getType();
      if (!connectionType.equals(connectorType)) {
        continue;
      }

      ConnectorService connectorService = connection.getConnectorService();
      ConnectorCategory category = connectorService.getRootCategory();
      if (categoryId != null) {
        category = connectorService.getCategory(categoryId);
      }

      ConnectorSearchResult<ConnectorEntity> connectorResult = connectorService.search(category, query, searchType, Collections.emptyMap());
      searchResult.merge(connectorResult);
    }

    searchResult.getSearchResult().sort((o1, o2) -> o2.getClass().getName().compareTo(o1.getClass().getName()));
    return searchResult;
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

}
