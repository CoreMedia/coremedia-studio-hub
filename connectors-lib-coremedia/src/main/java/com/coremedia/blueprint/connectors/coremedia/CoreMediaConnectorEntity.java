package com.coremedia.blueprint.connectors.coremedia;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.results.BulkOperationResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract public class CoreMediaConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;

  protected Content content;

  ConnectorCategory parent;
  CoreMediaConnectorServiceImpl service;


  CoreMediaConnectorEntity(CoreMediaConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Content content, ConnectorId connectorId) {
    this.service = service;
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
    this.content = content;
    if(content != null) {  //ROOT
      this.name = content.getName();
    }
  }

  public CoreMediaConnectorServiceImpl getService() {
    return service;
  }

  public Content getContent() {
    return content;
  }

  @Override
  public Boolean isDeleteable() {
    return service.isDeleteable(content);
  }



  @Override
  public Boolean delete() {
    BulkOperationResult delete = content.delete();
    return delete.isSuccessful();
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

  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nonnull
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
