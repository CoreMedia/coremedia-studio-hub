package com.coremedia.blueprint.connectors.canto.rest.services;

import com.coremedia.blueprint.connectors.canto.rest.FieldKeys;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCatalogEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCatalogsSearchResultEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCategoryEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.SearchResultEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataService extends AbstractCantoService {

  @Cacheable(value = "cantoCatalogCache", cacheManager = "cacheManagerCanto")
  public List<CantoCatalogEntity> getCatalogs() {
    CantoCatalogsSearchResultEntity result = connector.performGet("/metadata/getcatalogs?serveraddress=localhost", CantoCatalogsSearchResultEntity.class);
    return result.getCatalogs();
  }

  public CantoCategoryEntity getRootCategory(String catalogId) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);
    return connector.performGet("/metadata/getcategories/{catalog}/categories", pathParams, null, CantoCategoryEntity.class);
  }

  public List<CantoCategoryEntity> getTopCategories(String catalogId) {
    CantoCategoryEntity rootCategory = getRootCategory(catalogId);
    return rootCategory.getSubCategories();
  }

  @Cacheable(value = "cantoCategoryCache", key = "#catalogId + '_' + #categoryId", cacheManager = "cacheManagerCanto")
  public CantoCategoryEntity getCategoryById(String catalogId, int categoryId) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add(CATEGORY_ID, Integer.toString(categoryId));

    return connector.performGet("/metadata/getcategories/{catalog}/categories", pathParams, queryParams, CantoCategoryEntity.class);
  }

  @Cacheable(value = "cantoCategoryAssignmentsCache", key = "#catalogId + '_' + #categoryId", cacheManager = "cacheManagerCanto")
  public List<AssetEntity> getAssignedAssets(String catalogId, int categoryId) {
    List<AssetEntity> assets = new ArrayList<>();

    SearchResultEntity searchResult = null;
    if (categoryId == EXTERNAL_ROOT_CATEGORY_ID) {
      // Get unassigned assets
      searchResult = search(catalogId, "Categories+!*");
    } else {
      searchResult = quickSearch(catalogId, ":" + categoryId + ":");
    }

    if (searchResult != null && searchResult.getTotalCount() > 0) {
      for (int assetId : searchResult.getIds()) {
        // Fetch asset data
        AssetEntity asset = getAssetById(catalogId, assetId);
        if (asset != null) {
          assets.add(asset);
        }
      }
    }

    return assets;
  }

  @Cacheable(value = "cantoSearchResultCache", key = "'search_' + #catalogId + '_' + #query", cacheManager = "cacheManagerCanto")
  public SearchResultEntity search(String catalogId, String query) {
    return doSearchInternal(catalogId, query, false);
  }

  @Cacheable(value = "cantoSearchResultCache", key = "'quicksearch_' + #catalogId + '_' + #query", cacheManager = "cacheManagerCanto")
  public SearchResultEntity quickSearch(String catalogId, String query) {
    return doSearchInternal(catalogId, query, true);
  }

  private SearchResultEntity doSearchInternal(String catalogId, String query, boolean quickSearch) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    if (quickSearch) {
      queryParams.add(QUICK_SEARCH_STRING, query);
    } else {
      queryParams.add(QUERY_STRING, query);
    }

    return connector.performPost("/metadata/search/{catalog}", pathParams, queryParams, SearchResultEntity.class);
  }

  @Cacheable(value = "cantoAssetCache", key = "#catalogId + '_' + #assetId", cacheManager = "cacheManagerCanto")
  public AssetEntity getAssetById(String catalogId, int assetId) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);
    pathParams.put(ASSET, Integer.toString(assetId));

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add(FIELD, FieldKeys.ASSET_NAME.nameWithAlias());
    queryParams.add(FIELD, FieldKeys.DESCRIPTION.nameWithAlias());
    queryParams.add(FIELD, FieldKeys.STATUS.nameWithAlias());
    queryParams.add(FIELD, FieldKeys.NOTES.nameWithAlias());
    queryParams.add(FIELD, FieldKeys.ASSET_DATA_SIZE.nameWithAlias());
    queryParams.add(FIELD, FieldKeys.ASSET_MODIFICATION_DATE.nameWithAliasAndSubField("format=yyyy-MM-dd'T'HH:mm:ssZ"));
    queryParams.add(FIELD, FieldKeys.FILE_FORMAT.nameWithAlias());
    queryParams.add(FIELD, FieldKeys.RATING.nameWithAlias());

    return connector.performGet("/metadata/getfieldvalues/{catalog}/{asset}", pathParams, queryParams, AssetEntity.class);
  }


}
