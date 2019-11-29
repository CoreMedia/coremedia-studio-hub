package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.celum.rest.Asset;
import com.coremedia.blueprint.connectors.celum.rest.Assets;
import com.coremedia.blueprint.connectors.celum.rest.Binary;
import com.coremedia.blueprint.connectors.celum.rest.CelumRestConnector;
import com.coremedia.blueprint.connectors.celum.rest.Node;
import com.coremedia.blueprint.connectors.celum.rest.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.LinkedMultiValueMap;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CelumCoraService {
  private static final Logger LOG = LoggerFactory.getLogger(CelumCoraService.class);
  private static final String MAX_SEARCH_RESULTS = "100";

  private CelumRestConnector connector;

  //---------------- Service Methods -----------------------------------------------------------------------------------

  @Cacheable(value = "celumSearchResultCache", key = "'search_' + #connectionId + '_' + #term", cacheManager = "cacheManagerCelum")
  public List<Asset> search(String connectionId, String term) {
    LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    if (term.equals("*")) {
      return Collections.emptyList();
    }

    try{
      int id = Integer.parseInt(term);
      Asset asset = getAsset(connectionId, id);
      if(asset != null) {
        return Arrays.asList(asset);
      }
    }
    catch (Exception e) {
      //ignore, not an id or no match
    }

    //ensure URL format
    term = term.replaceAll("\"", "");
    term = term.replaceAll("$", "");
    term = term.replaceAll("&", " ");

    queryParams.add("$search", "\"" + term + "\"");
    queryParams.add("$top", MAX_SEARCH_RESULTS);
    queryParams.add("$orderby", "name");
    queryParams.add("$expand", "type,nodes");

    return connector.performGet("Assets", Assets.class, queryParams).getValue();
  }

  @Cacheable(value = "celumCategoryCache", key = "#connectionId + '_root'", cacheManager = "cacheManagerCelum")
  public List<Node> getTopLevelCategories(String connectionId) {
    String query = "Nodes?$expand=parent,children,assets,type&$filter=parent eq null";
    Nodes childNodes = connector.performGet(query, Nodes.class, null);
    return childNodes.getValue();
  }

  @Cacheable(value = "celumCategoryCache", key = "#connectionId + '_' + #id", cacheManager = "cacheManagerCelum")
  public Node getCategory(String connectionId, Integer id) {
    String query = "Nodes(" + id + ")?$expand=parent,children,assets,type";
    return connector.performGet(query, Node.class, null);
  }

  @Cacheable(value = "celumAssetCache", key = "#connectionId + '_' + #id", cacheManager = "cacheManagerCelum")
  public Asset getAsset(String connectionId, int id) {
    LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    //queryParams.add("$expand", "informationFieldValues/servicetags");
    queryParams.add("$expand", "type,nodes");
    return connector.performGet("Assets(" + id + ")", Asset.class, queryParams);
  }

  public Binary getBinary(Asset asset, int formatId) {
    int versionId = asset.getVersionInformation().getVersionId();
    return connector.performGet("Binaries(asset=" + asset.getId() + ",version=" + versionId + ",type='download',format='" + formatId + "')", Binary.class, null);
  }

  public InputStream stream(Asset asset, int formatId) {
    int versionId = asset.getVersionInformation().getVersionId();
    String path = "Binaries(asset=" + asset.getId() + ",version=" + versionId + ",type='download',format='" + formatId + "')/$value";
    return connector.stream(path);
  }

  public InputStream download(Binary binary) {
    String mediaLink = binary.getMediaLink();
    return connector.streamUrl(mediaLink);
  }

  //---------------- Misc ----------------------------------------------------------------------------------------------

  public void setConnector(CelumRestConnector connector) {
    this.connector = connector;
  }
}
