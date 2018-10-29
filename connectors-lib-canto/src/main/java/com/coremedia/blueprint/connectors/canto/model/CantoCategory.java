package com.coremedia.blueprint.connectors.canto.model;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.canto.CantoConnectorServiceImpl;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCategoryEntity;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.coremedia.blueprint.connectors.canto.rest.services.AbstractCantoService.EXTERNAL_ROOT_CATEGORY_ID;

public class CantoCategory extends CantoConnectorEntity implements ConnectorCategory {
  private static final Logger LOG = LoggerFactory.getLogger(CantoCategory.class);

  private static final String DISPLAY_NAME = "displayName";

  private CantoCategoryEntity delegate;

  public CantoCategory(@NonNull CantoConnectorServiceImpl connectorService) {
    this(null, connectorService);
  }

  public CantoCategory(ConnectorId connectorId, @NonNull CantoConnectorServiceImpl connectorService) {
    super(connectorId, connectorService);

    if (connectorId == null || connectorId.isRootId()) {
      super.connectorId = ConnectorId.createRootId(getContext().getConnectionId());  // No id means root
      this.delegate = getMetadataService().getCategoryById(getCatalogId(), EXTERNAL_ROOT_CATEGORY_ID);
    } else {
      this.delegate = getMetadataService().getCategoryById(getCatalogId(), Integer.parseInt(connectorId.getExternalId()));
    }

    if(this.delegate == null) {
      LOG.error("Failed to initialize " + connectorId + ": no delegate found");
    }
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    List<ConnectorCategory> subCategories = new ArrayList<>();

    if(delegate == null) {
      LOG.warn("No delegate found for " + this + ", skipping subcategories");
    }
    else if (delegate.hasSubCategories()) {
      for (CantoCategoryEntity subEntity : delegate.getSubCategories()) {
        ConnectorId categoryId = ConnectorId.createCategoryId(getContext().getConnectionId(), Integer.toString(subEntity.getId()));
        CantoCategory cantoCategory = (CantoCategory) connectorService.getCategory(getContext(), categoryId);
        if(cantoCategory.getDelegate() != null) {
          subCategories.add(cantoCategory);
        }

      }
    }

    return subCategories;
  }

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    List<ConnectorItem> children = new ArrayList<>();

    try {
      int categoryId = connectorId.isRootId() ? EXTERNAL_ROOT_CATEGORY_ID : Integer.parseInt(connectorId.getExternalId());
      List<AssetEntity> assetEntities = getMetadataService().getAssignedAssets(getCatalogId(), categoryId);
      if (CollectionUtils.isNotEmpty(assetEntities)) {
        for (AssetEntity a : assetEntities) {
          CantoAsset cantoAsset = new CantoAsset(this, a, connectorService);
          children.add(cantoAsset);
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to retrieve items for " + this + ": " + e.getMessage(), e);
    }

    return children;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return () -> {
      Map<String, Object> result = new TreeMap<>();
      result.put("id", delegate.getId());
      return result;
    };
  }

  @Override
  public boolean isWriteable() {
    return true;
  }

  @NonNull
  @Override
  public ConnectorId getConnectorId() {
    return connectorId;
  }


  @NonNull
  @Override
  public String getName() {
    if (connectorId.isRootId()) {
      return getContext().getProperty(DISPLAY_NAME);
    }
    return getDelegate().getName();
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return null;
  }

  @NonNull
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
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  public CantoCategoryEntity getDelegate() {
    return delegate;
  }

  public void setDelegate(CantoCategoryEntity delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return connectorService.refresh(context, this);
  }

  @Override
  public ConnectorItem upload(@NonNull ConnectorContext context, String itemName, InputStream inputStream) {
    return connectorService.upload(context, this, itemName, inputStream);
  }
}
