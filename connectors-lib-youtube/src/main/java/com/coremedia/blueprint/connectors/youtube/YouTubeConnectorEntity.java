package com.coremedia.blueprint.connectors.youtube;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Date;

public class YouTubeConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;
  private ConnectorCategory parent;

  YouTubeConnectorEntity(ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId) {
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
  }

  @Override
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  public Date getLastModified() {
    return null;
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
