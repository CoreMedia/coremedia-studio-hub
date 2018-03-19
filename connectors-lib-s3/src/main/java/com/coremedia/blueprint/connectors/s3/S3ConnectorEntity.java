package com.coremedia.blueprint.connectors.s3;


import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

public class S3ConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;
  private ConnectorCategory parent;

  S3ConnectorServiceImpl s3Service;
  S3ObjectSummary s3ObjectSummary;

  S3ConnectorEntity(S3ConnectorServiceImpl s3Service, ConnectorCategory parent, ConnectorContext context, S3ObjectSummary summary, ConnectorId connectorId) {
    this.s3Service = s3Service;
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
    this.s3ObjectSummary = summary;
    if(summary != null) {
      this.name = S3Helper.formatName(summary);
    }
  }

  public String getKey() {
    if(s3ObjectSummary != null) {
      return s3ObjectSummary.getKey();
    }
    return null;
  }

  String getBucketName() {
    return s3ObjectSummary.getBucketName();
  }

  @Override
  public Boolean isDeleteable() {
    return this instanceof ConnectorItem;
  }

  @Override
  public Boolean delete() {
    return s3Service.delete((S3ConnectorItem)this);
  }

  public Date getLastModified() {
    if(s3ObjectSummary != null) {
      return s3ObjectSummary.getLastModified();
    }
    return null;
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
