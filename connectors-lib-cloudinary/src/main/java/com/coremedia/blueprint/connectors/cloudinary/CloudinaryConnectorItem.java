package com.coremedia.blueprint.connectors.cloudinary;

import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CloudinaryConnectorItem extends CloudinaryConnectorEntity implements ConnectorItem {
  private static final Logger LOG = LoggerFactory.getLogger(CloudinaryConnectorItem.class);

  private CloudinaryAsset asset;
  private CloudinaryConnectorCategory category;
  private CloudinaryConnectorServiceImpl service;

  CloudinaryConnectorItem(ConnectorId id, ConnectorContext context, CloudinaryConnectorServiceImpl service, CloudinaryAsset asset, CloudinaryConnectorCategory category) {
    super(context, id);
    this.service = service;
    this.asset = asset;
    this.category = category;
  }

  @Override
  public long getSize() {
    return asset.getSize();
  }

  @Nullable
  @Override
  public String getDescription() {
    return asset.getName();
  }

  @Nullable
  @Override
  public InputStream stream() {
    try {
      return service.stream(asset);
    } catch (Exception e) {
      LOG.error("Failed to open resource input stream for " + asset.getName() + "/" + asset.getId() + ": " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();

    data.put("height", asset.getHeight());
    data.put("width", asset.getWidth());
    data.put("folder", asset.getFolder());
    data.put("format", asset.getFormat());

    return () -> data;
  }

  @NonNull
  @Override
  public String getItemType() {
   return asset.getConnectorItemType(context);
  }

  @Override
  public String getMimeType() {
    return asset.getMimeType(context);
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @NonNull
  @Override
  public String getName() {
    return asset.getName();
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return category;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    String itemType = getItemType();
    if (itemType.equals(DEFAULT_TYPE)) {
      return getName();
    }
    return asset.getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return asset.getLastModificationDate();
  }


  @Nullable
  @Override
  public String getThumbnailUrl() {
    if(asset.getResourceType().equals("image") && this.getMimeType().startsWith("image")) {
      return ConnectorItem.super.getStreamUrl();
    }
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    try {
      String resourceType = asset.getResourceType();
      return ASSET_BASE_URL + resourceType + "/upload/" + URLEncoder.encode(getConnectorId().getExternalId(), "utf8");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  @Override
  public boolean isDeleteable() {
    return true;
  }

  @Override
  public boolean delete() {
    return service.delete(context, asset);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CloudinaryConnectorItem) {
      CloudinaryConnectorItem item = (CloudinaryConnectorItem) obj;
      return item.asset.getId().equals(this.asset.getId());
    }
    return false;
  }
}
