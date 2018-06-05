package com.coremedia.blueprint.connectors.canto.model;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.canto.CantoConnectorServiceImpl;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CantoAsset extends CantoConnectorEntity implements ConnectorItem {

  private AssetEntity delegate;
  private ConnectorCategory parent;

  public CantoAsset(@Nonnull ConnectorCategory category, @Nonnull AssetEntity delegate, @Nonnull CantoConnectorServiceImpl connectorService) {
    super(ConnectorId.createItemId(category.getConnectorId(), Integer.toString(delegate.getId())), connectorService);
    this.parent = category;
    this.delegate = delegate;
  }

  @Override
  public long getSize() {
    if (delegate != null && delegate.getDataSize() != null) {
      return delegate.getDataSize().getValue();
    }

    return 0L;
  }

  @Nullable
  @Override
  public String getDescription() {
    return delegate.getDescription();
  }

  @Nullable
  @Override
  public InputStream stream() {
    return getConnectorService().getAssetService().streamAsset(getCatalogId(), Integer.parseInt(getConnectorId().getExternalId()));
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return () -> {
      Map<String, Object> result = new TreeMap<>();
      result.put("id", delegate.getId());
      result.put("type", delegate.getFileFormat());
      result.put("status", getMimeType());
      //result.put("alt text", asset.getAltText());
      //result.put("keywords", asset.getKeywords().stream().collect(Collectors.joining(",")));
      result.put("description", delegate.getDescription());

      if (delegate.getRating() != null) {
        result.put("rating", delegate.getRating().getDisplayString());
      }

      result.put("notes", delegate.getNotes());
      return result;
    };
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @Nonnull
  @Override
  public String getName() {
    return delegate.getName();
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return delegate.getModificationDate();
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("catalog", getCatalogId());
    pathParams.put("record", Integer.toString(delegate.getId()));
    return buildManagementUrl("/{catalog}/content/view/records={record}", pathParams, null);
  }

  @Override
  public Boolean isDeleteable() {
    return true;
  }

  @Override
  public Boolean delete() {
    return getConnectorService().getAssetService().deleteAsset(getCatalogId(), delegate.getId());
  }
}
