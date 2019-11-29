package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumnValue;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorItemTypes;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.celum.rest.Asset;
import com.coremedia.blueprint.connectors.celum.rest.FileProperty;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumnValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class CelumConnectorItem extends CelumConnectorEntity implements ConnectorItem {
  private static final Logger LOG = LoggerFactory.getLogger(CelumConnectorItem.class);

  private Asset asset;
  private CelumConnectorCategory category;
  private CelumConnectorService service;

  CelumConnectorItem(ConnectorId id, ConnectorContext context, CelumConnectorService service, Asset asset, CelumConnectorCategory category) {
    super(context, id);
    this.service = service;
    this.asset = asset;
    this.category = category;
  }

  public Asset getAsset() {
    return asset;
  }

  @Override
  public long getSize() {
    return asset.getFileInformation().getOriginalFileSize();
  }

  @Nullable
  @Override
  public String getDescription() {
    return asset.getName();
  }

  @Override
  public List<ConnectorColumnValue> getColumnValues() {
    DefaultConnectorColumnValue v1 = new DefaultConnectorColumnValue(String.valueOf(asset.getId()), DATA_INDEX_CELUM_ID);
    return Arrays.asList(v1);
  }

  @Nullable
  @Override
  public String getThumbnailUrl() {
    String url = asset.getPreviewInformation().getThumbUrl();
    if(url != null) {
      return url;
    }
    return null;
  }

  @Nullable
  @Override
  public InputStream stream() {
    try {
      return service.stream(asset, context);
    } catch (Exception e) {
      LOG.error("Failed to open resource input stream for " + asset.getName() + "/" + asset.getId() + ": " + e.getMessage(), e);
    }
    return null;
  }

  public InputStream download() {
    try {
      return service.download(asset, context);
    } catch (Exception e) {
      LOG.error("Failed to open resource input stream for " + asset.getName() + "/" + asset.getId() + ": " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    String language = Locale.getDefault().getLanguage();
    if (asset.getType() != null) {
      data.put("asset_type", asset.getType().getLabel(language));
    }


    data.put("Celum Id", asset.getId());
    List<FileProperty> fileProperties = asset.getFileProperties();
    for (FileProperty fileProperty : fileProperties) {
      String value = fileProperty.getValue();
      if (!StringUtils.isEmpty(value)) {
        data.put(fileProperty.getName(), fileProperty.getValue());
      }
    }
    return () -> data;
  }

  @NonNull
  @Override
  public String getItemType() {
    String fileType = asset.getFileInformation().getFileExtension();
    ConnectorItemTypes itemTypes = context.getItemTypes();
    if (itemTypes != null) {
      String typeForName = itemTypes.getTypeForName(fileType);
      if (typeForName != null) {
        return typeForName;
      }
    }
    return DEFAULT_TYPE;
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @NonNull
  @Override
  public String getName() {
    String ext = asset.getFileInformation().getFileExtension();
    if (!asset.getName().endsWith(ext)) {
      return asset.getName() + "." + ext;
    }
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
    return asset.getModificationInformation().getLastModificationDate();
  }

  @Nullable
  @Override
  public String getManagementUrl() {
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

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CelumConnectorItem) {
      CelumConnectorItem item = (CelumConnectorItem) obj;
      return item.asset.getId() == this.asset.getId();
    }
    return false;
  }
}
