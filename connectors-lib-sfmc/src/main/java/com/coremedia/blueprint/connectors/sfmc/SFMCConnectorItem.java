package com.coremedia.blueprint.connectors.sfmc;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.sfmc.rest.AssetMapping;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCAsset;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class SFMCConnectorItem extends SFMCConnectorEntity implements ConnectorItem {
  private SFMCAsset asset;

  SFMCConnectorItem(SFMCConnectorServiceImpl service, ConnectorCategory parent, SFMCAsset asset, ConnectorContext context, ConnectorId connectorId) {
    super(service, parent, asset, context, connectorId);
    this.asset = asset;
  }

  @Override
  public Date getLastModified() {
    return asset.getModifiedDate();
  }

  @Nullable
  @Override
  public String getStreamUrl() {
    //check if thumbnail is available
    InputStream stream = stream();
    if (stream() != null) {
      try {
        stream.close();
      } catch (IOException e) {
        //
      }
      return ConnectorItem.super.getStreamUrl();
    }
    return asset.getFileProperties().getPublishedURL();
  }

  @Override
  public long getSize() {
    if (asset.getFileProperties() != null) {
      return asset.getFileProperties().getFileSize();
    }

    return 0;
  }

  @Nullable
  @Override
  public String getDescription() {
    return asset.getDescription();
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return () -> {
      Map<String, Object> metaData = new LinkedHashMap<>();
      metaData.put("assetType", AssetMapping.forType(asset.getAssetType().getId()));
      metaData.put("status", asset.getStatus().getName());

      if (asset.getFileProperties() != null) {
        metaData.put("publishedUrl", asset.getFileProperties().getPublishedURL());

        if (asset.getFileProperties().getHeight() > 0) {
          metaData.put("width", asset.getFileProperties().getWidth());
          metaData.put("height", asset.getFileProperties().getHeight());
        }
      }

      metaData.put("owner", asset.getOwner().getName() + " (" + asset.getOwner().getEmail() + ")");
      metaData.put("createdBy", asset.getCreatedBy().getName() + " (" + asset.getCreatedBy().getEmail() + ")");
      metaData.put("modifiedBy", asset.getModifiedBy().getName() + " (" + asset.getModifiedBy().getEmail() + ")");
      return metaData;
    };
  }

  @NonNull
  @Override
  public String getItemType() {
    return AssetMapping.forType(asset.getAssetType().getId());
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return service.stream(getContext(), getConnectorId().getExternalId());
  }

  @Nullable
  @Override
  public InputStream download() {
    return service.download(getContext(), getConnectorId().getExternalId());
  }
}
