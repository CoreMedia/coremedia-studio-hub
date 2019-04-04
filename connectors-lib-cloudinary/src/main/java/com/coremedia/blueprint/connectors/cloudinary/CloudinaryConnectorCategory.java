package com.coremedia.blueprint.connectors.cloudinary;

import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryFolder;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.activation.MimeType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CloudinaryConnectorCategory extends CloudinaryConnectorEntity implements ConnectorCategory {

  private CloudinaryFolder folder;
  private String name;
  private ConnectorCategory parent;
  private List<ConnectorCategory> childCategories;
  private List<ConnectorItem> childItems = new ArrayList<>();
  private CloudinaryConnectorServiceImpl service;

  CloudinaryConnectorCategory(CloudinaryConnectorServiceImpl service, ConnectorContext context, ConnectorId id, String name, List<ConnectorCategory> childCategories) {
    super(context, id);
    this.name = name;
    this.childCategories = childCategories;
    this.service = service;
  }

  CloudinaryConnectorCategory(CloudinaryConnectorServiceImpl service, ConnectorContext context, ConnectorId id, CloudinaryFolder folder, ConnectorCategory parent, List<ConnectorCategory> childCategories) {
    super(context, id);
    this.service = service;
    this.folder = folder;
    this.parent = parent;
    this.childCategories = childCategories;
  }

  public CloudinaryFolder getFolder() {
    return folder;
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return childCategories;
  }

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    return childItems;
  }

  @Override
  public boolean isWriteable() {
    return true;
  }


  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    if (getFolder() != null) {
      data.put("path", "/" + getFolder().getFolder());
    }
    else {
      data.put("path", "/");
    }
    return () -> data;
  }

  @NonNull
  @Override
  public String getName() {
    if (name != null) {
      return name;
    }
    return folder.getName();
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    if (folder != null) {
      return folder.getLastModified();
    }
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    String url = FOLDER_BASE_URL + ASSET_TYPE_IMAGES;
    if (getFolder() != null) {
      url = url + "/" + getFolder().getFolder();
    }
    return url;
  }

  @Override
  public boolean isDeleteable() {
    return true;
  }

  @Override
  public boolean delete() {
    return false;
  }

  public void setChildItems(List<ConnectorItem> childItems) {
    this.childItems = childItems;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return service.refresh(context, this);
  }

  @Override
  public ConnectorItem upload(@NonNull ConnectorContext context, @NonNull String itemName, @NonNull MimeType mimeType, @NonNull InputStream inputStream) {
    return service.upload(context, this, itemName, inputStream);
  }
}
