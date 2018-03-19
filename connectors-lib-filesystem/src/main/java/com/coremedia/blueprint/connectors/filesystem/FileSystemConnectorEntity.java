package com.coremedia.blueprint.connectors.filesystem;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

public class FileSystemConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;
  private ConnectorCategory parent;
  protected File file;

  FileSystemConnectorEntity(ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, File file) {
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
    this.name = file.getName();
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  @Override
  public Boolean isDeleteable() {
    return true;
  }

  @Override
  public Boolean delete() {
    if (file.isFile()) {
      return file.delete();
    }

    return false;
  }

  public Date getLastModified() {
    return new Date(getFile().lastModified());
  }

  @Override
  public String getConnectorType() {
    return context.getType();
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  public void setContext(ConnectorContext context) {
    this.context = context;
  }

  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  public void setParent(ConnectorCategory parent) {
    this.parent = parent;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  public ConnectorId getConnectorId() {
    return connectorId;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }
}
