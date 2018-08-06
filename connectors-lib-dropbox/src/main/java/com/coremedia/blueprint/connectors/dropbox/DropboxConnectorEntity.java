package com.coremedia.blueprint.connectors.dropbox;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.dropbox.core.v2.files.Metadata;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

abstract public class DropboxConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;

  ConnectorCategory parent;
  DropboxConnectorServiceImpl service;

  DropboxConnectorEntity(DropboxConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Metadata metadata, ConnectorId connectorId) {
    this.service = service;
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
    if(metadata != null) {  //ROOT
      this.name = metadata.getName();
    }
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

  @Override
  public ConnectorCategory getParent() {
    return parent;
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
