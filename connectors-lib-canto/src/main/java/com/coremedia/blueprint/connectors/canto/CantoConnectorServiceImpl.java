package com.coremedia.blueprint.connectors.canto;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.canto.model.CantoAsset;
import com.coremedia.blueprint.connectors.canto.model.CantoCategory;
import com.coremedia.blueprint.connectors.canto.rest.CantoConnector;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.SearchResultEntity;
import com.coremedia.blueprint.connectors.canto.rest.services.AssetService;
import com.coremedia.blueprint.connectors.canto.rest.services.MetadataService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CantoConnectorServiceImpl implements ConnectorService {

  private static final Logger LOG = LoggerFactory.getLogger(CantoConnectorServiceImpl.class);

  private static final String ASSETS_ID = "assets";
  private static final String TYPE_ASSET = "asset";
  private static final String CONTENTS_ID = "contents";
  private static final String TYPE_CONTENT = "content";
  private static final String HOST = "host";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String CATALOG = "catalog";

  private CantoCategory rootCategory;
  private ConnectorContext context;
  private CantoConnector restConnector;
  private MetadataService metadataService;
  private AssetService assetService;
  private String catalogId;


  @Override
  public boolean init(@Nonnull ConnectorContext context) {
    this.context = context;

    String host = context.getProperty(HOST);
    String username = context.getProperty(USERNAME);
    String password = context.getProperty(PASSWORD);
    catalogId = context.getProperty(CATALOG);

    if (StringUtils.isEmpty(host) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      throw new ConnectorException("No login data configured for Canto Cumulus connection " + context.getConnectionId() + "  Check if 'host', 'username' and 'password' are set");
    }

    // Create Connector
    restConnector = new CantoConnector(host, username, password);

    // Init Services
    metadataService.setConnector(restConnector);
    assetService.setConnector(restConnector);

    return true;
  }


  // --- Category ---
  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    rootCategory = new CantoCategory(this);
    return rootCategory;
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    return new CantoCategory(id, this);
  }


  // --- Item ---
  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    AssetEntity a = metadataService.getAssetById(getCatalogId(), Integer.parseInt(id.getExternalId()));
    ConnectorCategory parent = getRootCategory(context);
    if(!id.isRootId()) {
      parent = getCategory(context, id);
    }
    return new CantoAsset(parent, a, this);
  }


  // --- Search ---

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();

    SearchResultEntity searchResult = metadataService.quickSearch(getCatalogId(), query);

    if (searchResult != null && searchResult.getTotalCount() > 0) {
      for (int assetId : searchResult.getIds()) {
        AssetEntity asset = metadataService.getAssetById(catalogId, assetId);
        if (asset != null) {
          results.add(new CantoAsset(category, asset, this));
        }
      }
    }

    return new ConnectorSearchResult<>(results);
  }

  @CacheEvict(cacheNames = {"cantoCategoryCache", "cantoCategoryAssignmentsCache"}, key = "#root.target.catalogId + '_' + #category.connectorId.externalId", beforeInvocation = true, cacheManager = "cacheManagerCanto")
  public Boolean refresh(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category) {
    try {
      if (category instanceof CantoCategory) {
        CantoCategory cat = (CantoCategory) category;
        int externalId = Integer.parseInt(category.getConnectorId().getExternalId());
        cat.setDelegate(metadataService.getCategoryById(catalogId, externalId));
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  @CacheEvict(cacheNames = {"cantoCategoryCache", "cantoCategoryAssignmentsCache"}, key = "#root.target.catalogId + '_' + #category.connectorId.externalId", cacheManager = "cacheManagerCanto")
  public ConnectorItem upload(@Nonnull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    int categoryId = -1;
    if (category != null) {
      categoryId = Integer.parseInt(category.getConnectorId().getExternalId());
    }
    int assetId = assetService.uploadAsset(getCatalogId(), categoryId, itemName, inputStream);

    CantoAsset createdAsset = null;
    if (assetId > -1) {
      AssetEntity assetEntity = metadataService.getAssetById(getCatalogId(), assetId);
      createdAsset = new CantoAsset(category, assetEntity, this);
    }
    return createdAsset;
  }

  public ConnectorContext getContext() {
    return context;
  }

  public CantoConnector getRestConnector() {
    return restConnector;
  }

  public MetadataService getMetadataService() {
    return metadataService;
  }

  @Resource(name = "cantoMetaDataService")
  public void setMetadataService(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  public AssetService getAssetService() {
    return assetService;
  }

  @Resource(name = "cantoAssetService")
  public void setAssetService(AssetService assetService) {
    this.assetService = assetService;
  }

  public String getCatalogId() {
    return catalogId;
  }
}
