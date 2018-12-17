package com.coremedia.blueprint.studio.connectors.service {
import com.coremedia.blueprint.studio.connectors.model.*;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.coremedia.ui.util.ObjectUtils;

/**
 * Utility class for accessing the ConnectorServiceResource
 */
public class ConnectorService {
  private static const ALL_CONNECTIONS_INDICATOR:String = "all";

  /**
   * The search call triggered for the selected system/connection
   * @param connectionId the connection id which may be null if the root node was selected
   * @param searchParameters
   * @param connectorType the type of the system to execute the search in
   * @param callback callback function
   */
  public static function search(connectorType:String, connectionId:String, searchParameters:SearchParameters, callback:Function):void {
    var conId:String = connectionId || ALL_CONNECTIONS_INDICATOR;
    var searchMethod:RemoteServiceMethod = new RemoteServiceMethod("connector/service/search/" + connectorType + "/" + conId + "/" + editorContext.getSitesService().getPreferredSiteId(), "GET");

    //to object conversion
    var searchParams:Object = ObjectUtils.getPublicProperties(searchParameters);
    searchParams = ObjectUtils.removeUndefinedOrNullProperties(searchParams);

    searchMethod.request(searchParams,
            function (response:RemoteServiceMethodResponse):void {
              var searchResult:SearchResult = new SearchResult();
              var responseObject:Object = response.getResponseJSON();
              searchResult.setHits(responseObject['hits']);
              searchResult.setTotal(responseObject['total']);
              callback.call(null, searchResult);
            });
  }

  public static function getSearchSuggestionsUrl(connectorType:String, connectionId:String):String {
    return "/api/service/suggestions/" + connectorType + "/" + connectionId + "/" + editorContext.getSitesService().getPreferredSiteId();
  }
}
}
