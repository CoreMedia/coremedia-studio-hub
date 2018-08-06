package com.coremedia.blueprint.connectors.dropbox;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DropboxConnectorCategory extends DropboxConnectorEntity implements ConnectorCategory {

  private static final String DROPBOX_URL = "https://www.dropbox.com/home/";

  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();
  private FolderMetadata folderMetadata;

  DropboxConnectorCategory(DropboxConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Metadata metadata, ConnectorId id) {
    super(service, parent, context, metadata, id);
    this.folderMetadata = (FolderMetadata) metadata;
  }

  @Override
  public String getType() {
    if(getConnectorId().isRootId()) {
      return "dropbox";
    }
    return ConnectorCategory.super.getType();
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    String url = DROPBOX_URL;
    if(service.getAppName() != null) {
      url = url + "Apps/" + service.getAppName();
    }

    if(folderMetadata != null) {
      return url + folderMetadata.getPathLower();
    }

    return url;
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    return items;
  }

  @Override
  public boolean isWriteable() {
    return true;
  }

  @Nullable
  @Override
  public Date getLastModified() {
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

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return service.refresh(context, this);
  }

  @Override
  public ConnectorItem upload(@NonNull ConnectorContext context, String itemName, InputStream inputStream) {
   return service.upload(context, this, itemName, inputStream);
  }
}
