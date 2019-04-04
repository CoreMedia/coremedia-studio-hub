package com.coremedia.blueprint.connectors.filesystem;


import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.api.ConnectorId;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
  public boolean isDeleteable() {
    return true;
  }

  @Override
  public boolean delete() {
    if (file.isFile()) {
      return file.delete();
    }

    return false;
  }

  public Date getLastModified() {
    return new Date(getFile().lastModified());
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
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

  @NonNull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @NonNull
  @Override
  public ConnectorId getConnectorId() {
    return connectorId;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }
}
