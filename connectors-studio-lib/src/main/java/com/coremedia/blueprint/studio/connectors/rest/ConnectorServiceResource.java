package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorSearchResultRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorSuggestionRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorSuggestionResultRepresentation;
import com.coremedia.cap.content.ContentType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "connector/service", produces = APPLICATION_JSON_VALUE)
public class ConnectorServiceResource {

  private static final String SEARCH_PARAM_QUERY = "query";
  private static final String SEARCH_PARAM_LIMIT = "limit";
  private static final String DEFAULT_LIMIT = "50";
  private static final String SEARCH_PARAM_ORDER_BY = "orderBy";
  private static final String SEARCH_PARAM_SEARCH_TYPE = "searchType";
  private static final String SEARCH_PARAM_CATEGORY = "category";

  private static final String SITE_ID_PARAM = "siteId";
  private static final String CONNECTOR_CONNECTION_ID = "connectorConnectionId";
  private static final String CONNECTOR_TYPE = "connectorType";

  private Connectors connector;
  private ConnectorContextProvider connectorContextProvider;

  public ConnectorServiceResource(Connectors connector, ConnectorContextProvider connectorContextProvider) {
    this.connector = connector;
    this.connectorContextProvider = connectorContextProvider;
  }

  @GetMapping("search/{connectorType}/{connectorConnectionId}/{siteId}")
  @Nullable
  public ConnectorSearchResultRepresentation search(@PathVariable(CONNECTOR_TYPE) String connectorType,
                                                    @PathVariable(CONNECTOR_CONNECTION_ID) String connectionId,
                                                    @PathVariable(SITE_ID_PARAM) String siteId,
                                                    @RequestParam(SEARCH_PARAM_QUERY) String query,
                                                    @RequestParam(value = SEARCH_PARAM_LIMIT, required = false, defaultValue = DEFAULT_LIMIT) int limit,
                                                    @RequestParam(value = SEARCH_PARAM_ORDER_BY, required = false) String orderBy,
                                                    @RequestParam(SEARCH_PARAM_CATEGORY) String categoryId,
                                                    @RequestParam(SEARCH_PARAM_SEARCH_TYPE) String searchType) {
    ConnectorId id = null;
    if (categoryId != null) {
      id = ConnectorId.toId(categoryId);
    }

    ConnectorSearchResult<ConnectorEntity> searchResult = search(query, connectorType, connectionId, siteId, id, searchType);
    return new ConnectorSearchResultRepresentation(searchResult.getSearchResult(), searchResult.getTotalCount());
  }

  @GetMapping("suggestions/{connectorType}/{connectorConnectionId}/{siteId}")
  @NonNull
  public ConnectorSuggestionResultRepresentation searchSuggestions(@PathVariable(CONNECTOR_TYPE) String connectorType,
                                                                   @PathVariable(CONNECTOR_CONNECTION_ID) String connectionId,
                                                                   @PathVariable(SITE_ID_PARAM) String siteId,
                                                                   @RequestParam(SEARCH_PARAM_QUERY) String query,
                                                                   @RequestParam(value = SEARCH_PARAM_LIMIT, required = false, defaultValue = DEFAULT_LIMIT) int limit,
                                                                   @RequestParam(SEARCH_PARAM_SEARCH_TYPE) String searchType,
                                                                   @RequestParam(SEARCH_PARAM_CATEGORY) String categoryId) {
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
    if (connectionId == null || connectionId.equals(ConnectorResource.siteDefault)) {
      List<ConnectorContext> contexts = connectorContextProvider.findContexts(siteId);
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
      ConnectorContext context = connection.getContext();
      ConnectorCategory category = connectorService.getRootCategory(context);
      if (categoryId != null) {
        category = connectorService.getCategory(context, categoryId);
      }

      ConnectorSearchResult<ConnectorEntity> connectorResult = connectorService.search(context, category, query, searchType, Collections.emptyMap());
      searchResult.merge(connectorResult);
    }

    searchResult.getSearchResult().sort((o1, o2) -> o2.getClass().getName().compareTo(o1.getClass().getName()));
    return searchResult;
  }

}
