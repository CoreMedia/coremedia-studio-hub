package com.coremedia.blueprint.connectors.dropbox;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  @Nonnull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  @Nonnull
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
  public Boolean isDeleteable() {
    return false;
  }

  @Override
  public Boolean delete() {
    return false;
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }
}
