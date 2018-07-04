package com.coremedia.blueprint.connectors.canto.model;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.canto.CantoConnectorServiceImpl;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCategoryEntity;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.connectors.canto.rest.services.AbstractCantoService.EXTERNAL_ROOT_CATEGORY_ID;

public class CantoCategory extends CantoConnectorEntity implements ConnectorCategory {
  private static final Logger LOG = LoggerFactory.getLogger(CantoCategory.class);

  private static final String DISPLAY_NAME = "displayName";

  private CantoCategoryEntity delegate;

  public CantoCategory(@Nonnull CantoConnectorServiceImpl connectorService) {
    this(null, connectorService);
  }

  public CantoCategory(ConnectorId connectorId, @Nonnull CantoConnectorServiceImpl connectorService) {
    super(connectorId, connectorService);

    if (connectorId == null || connectorId.isRootId()) {
      super.connectorId = ConnectorId.createRootId(getContext().getConnectionId());  // No id means root
      this.delegate = getMetadataService().getCategoryById(getCatalogId(), EXTERNAL_ROOT_CATEGORY_ID);
    } else {
      this.delegate = getMetadataService().getCategoryById(getCatalogId(), Integer.parseInt(connectorId.getExternalId()));
    }

  }

  @Nonnull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    List<ConnectorCategory> subCategories = new ArrayList<>();

    if(delegate == null) {
      LOG.warn("No delegate found for " + this + ", skipping subcategories");
    }
    else if (delegate.hasSubCategories()) {
      for (CantoCategoryEntity subEntity : delegate.getSubCategories()) {
        subCategories.add(new CantoCategory(ConnectorId.createCategoryId(getContext().getConnectionId(), Integer.toString(subEntity.getId())), connectorService));
      }
    }

    return subCategories;
  }

  @Nonnull
  @Override
  public List<ConnectorItem> getItems() {
    List<ConnectorItem> children = new ArrayList<>();

    try {
      int categoryId = connectorId.isRootId() ? EXTERNAL_ROOT_CATEGORY_ID : Integer.parseInt(connectorId.getExternalId());
      List<AssetEntity> assetEntities = getMetadataService().getAssignedAssets(getCatalogId(), categoryId);
      if (CollectionUtils.isNotEmpty(assetEntities)) {
        for (AssetEntity a : assetEntities) {
          children.add(new CantoAsset(this, a, connectorService));
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to retrieve items for " + this + ": " + e.getMessage(), e);
    }

    return children;
  }

  @Override
  public boolean isWriteable() {
    return true;
  }

  @Nonnull
  @Override
  public ConnectorId getConnectorId() {
    return connectorId;
  }


  @Nonnull
  @Override
  public String getName() {
    if (connectorId.isRootId()) {
      return getContext().getProperty(DISPLAY_NAME);
    }
    return delegate.getName();
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return null;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("catalog", getCatalogId());
    if(getDelegate() != null) {
      pathParams.put("category", Integer.toString(delegate.getId()));
      return buildManagementUrl("/{catalog}/container/view/categoryID={category}", pathParams, null);
    }
    return null;
  }

  @Override
  public Boolean isDeleteable() {
    return false;
  }

  @Override
  public Boolean delete() {
    return null;
  }

  public CantoCategoryEntity getDelegate() {
    return delegate;
  }

  public void setDelegate(CantoCategoryEntity delegate) {
    this.delegate = delegate;
  }

  @Override
  public Boolean refresh(@Nonnull ConnectorContext context) {
    return connectorService.refresh(context, this);
  }

  @Override
  public ConnectorItem upload(@Nonnull ConnectorContext context, String itemName, InputStream inputStream) {
    return connectorService.upload(context, this, itemName, inputStream);
  }
}
