package com.coremedia.blueprint.connectors.sfmc;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.sfmc.rest.SFMCConnector;
import com.coremedia.blueprint.connectors.sfmc.rest.SFMCService;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCAsset;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCAssetCollection;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCCategory;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCCategoryCollection;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SFMCConnectorServiceImpl implements ConnectorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SFMCConnectorServiceImpl.class);
  private SFMCService sfmcService;
  private SFMCConnectorCategory rootCategory;

  @Override
  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
    String clientId = context.getProperty(SFMCConnector.CLIENT_ID);
    String clientSecret = context.getProperty(SFMCConnector.CLIENT_SECRED);
    String subdomain = context.getProperty(SFMCConnector.SUBDOMAIN);

    if (StringUtils.isEmpty(clientId)
            || StringUtils.isEmpty(clientSecret)
            || StringUtils.isEmpty(subdomain)) {
      throw new ConnectorException("No credentials configured for SFMC connection " + context.getConnectionId());
    }

    return true;
  }

  @Override
  public void shutdown(@NonNull ConnectorContext context) throws ConnectorException {
    //nothing
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    Optional<SFMCAsset> asset = sfmcService.getAsset(context, Integer.parseInt(id.getExternalId()));

    SFMCConnectorCategory parent = rootCategory;
    if (!id.getParentId().isRootId()) {
      Optional<SFMCCategory> category = sfmcService.getCategory(context, Integer.parseInt(id.getParentId().getExternalId()));
      parent = createCategory(context, null, category.get(), false, false);
    }

    return new SFMCConnectorItem(this, parent, asset.get(), context, id);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    if (id.isRootId()) {
      return rootCategory;
    }

    Optional<SFMCCategory> category = sfmcService.getCategory(context, Integer.parseInt(id.getExternalId()));

    int parentId = category.get().getParentId();
    SFMCConnectorCategory connectorParent = null;
    if (parentId != 0) {
      Optional<SFMCCategory> parent = sfmcService.getCategory(context, parentId);
      connectorParent = createCategory(context, null, parent.get(), false, false);
    }

    return createCategory(context, connectorParent, category.get(), true, true);
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      Optional<SFMCCategoryCollection> categories = sfmcService.getCategories(context);
      for (SFMCCategory item : categories.get().getItems()) {
        if (item.isRoot()) {
          this.rootCategory = createCategory(context, null, item,  true, true);
          break;
        }
      }
    }

    return rootCategory;
  }

  @Override
  public InvalidationResult invalidate(@NonNull ConnectorContext context) {
    return null;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    ConnectorSearchResult<ConnectorEntity> result = new ConnectorSearchResult<>(Collections.emptyList());
    return result;
  }

  public boolean refresh(ConnectorContext context, SFMCConnectorCategory category) {
    this.rootCategory = null;
    this.sfmcService.invalidate();
    return true;
  }


  public void setSfmcService(SFMCService sfmcService) {
    this.sfmcService = sfmcService;
  }

  private SFMCConnectorCategory createCategory(@NonNull ConnectorContext context, @Nullable SFMCConnectorCategory parent, @NonNull SFMCCategory category,
                                               boolean resolveSubCategories, boolean resolveItems) {
    ConnectorId newCategoryId = ConnectorId.createCategoryId(context.getConnectionId(), category.getId());
    if (category.isRoot()) {
      newCategoryId = ConnectorId.createRootId(context.getConnectionId());
    }

    SFMCConnectorCategory connectorCategory = new SFMCConnectorCategory(this, parent, category, context, newCategoryId);
    if (resolveItems) {
      Optional<SFMCAssetCollection> assets = sfmcService.getAssets(context, category.getId());
      List<SFMCAsset> items = assets.get().getItems();
      for (SFMCAsset item : items) {
        ConnectorId id = ConnectorId.createItemId(newCategoryId, item.getId());
        ConnectorItem connectorItem = getItem(context, id);
        connectorCategory.getItems().add(connectorItem);
      }
    }

    if(resolveSubCategories) {
      Optional<SFMCCategoryCollection> categories = sfmcService.getCategories(context);
      for (SFMCCategory cat : categories.get().getItems()) {
        if (cat.getParentId() == category.getId()) {
          SFMCConnectorCategory subCat = createCategory(context, connectorCategory, cat, false, false);
          connectorCategory.getSubCategories().add(subCat);
        }
      }
    }

    return connectorCategory;
  }

  InputStream stream(ConnectorContext context, String externalId) {
    return sfmcService.getBinaryPreview(context, Integer.parseInt(externalId));
  }

  public InputStream download(ConnectorContext context, String externalId) {
    return sfmcService.getBinary(context, Integer.parseInt(externalId));
  }

  public String getThumbnailUrl(ConnectorContext context, SFMCAsset asset) {
    Optional<String> thumbnailUrl = sfmcService.getThumbnailUrl(context, asset);
    return thumbnailUrl.orElse(null);

  }
}
