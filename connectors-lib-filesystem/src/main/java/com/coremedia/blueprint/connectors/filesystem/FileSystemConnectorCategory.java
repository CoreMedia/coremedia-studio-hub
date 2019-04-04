package com.coremedia.blueprint.connectors.filesystem;

import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.activation.MimeType;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSystemConnectorCategory extends FileSystemConnectorEntity implements ConnectorCategory {

  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();
  private FileSystemConnectorServiceImpl service;

  FileSystemConnectorCategory(FileSystemConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, ConnectorId id, File file) {
    super(parent, context, id, file);
    this.service = service;
  }

  @Override
  public String getType() {
    if(getConnectorId().isRootId()) {
      return "filesystem";
    }
    return ConnectorCategory.super.getType();
  }

  @Override
  public Date getLastModified() {
    return new Date(file.lastModified());
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
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
    return file.canWrite();
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
  public ConnectorItem upload(@NonNull ConnectorContext context, @NonNull String itemName, @NonNull MimeType mimeType, @NonNull InputStream inputStream) {
    return service.upload(context, this, itemName, inputStream);
  }
}
