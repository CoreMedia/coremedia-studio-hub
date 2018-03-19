package com.coremedia.blueprint.connectors.filesystem;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSystemConnectorCategory extends FileSystemConnectorEntity implements ConnectorCategory {

  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();

  FileSystemConnectorCategory(ConnectorCategory parent, ConnectorContext context, ConnectorId id, File file) {
    super(parent, context, id, file);
  }

  @Nonnull
  @Override
  public List<ConnectorEntity> getChildren() {
    ArrayList<ConnectorEntity> children = new ArrayList<>();
    children.addAll(getSubCategories());
    children.addAll(getItems());
    return children;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
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
    return file.canWrite();
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }
}
