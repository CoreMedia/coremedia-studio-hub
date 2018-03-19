package com.coremedia.blueprint.connectors.s3;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class S3ConnectorCategory extends S3ConnectorEntity implements ConnectorCategory {

  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();

  S3ConnectorCategory(S3ConnectorServiceImpl s3Service, ConnectorCategory parent, ConnectorContext context, S3ObjectSummary summary, ConnectorId id) {
    super(s3Service, parent, context, summary, id);
  }

  @Nonnull
  @Override
  public List<ConnectorEntity> getChildren() {
    ArrayList<ConnectorEntity> connectorEntitiesBeans = new ArrayList<>();
    connectorEntitiesBeans.addAll(getSubCategories());
    connectorEntitiesBeans.addAll(getItems());
    return connectorEntitiesBeans;
  }

  @Override
  public String getType() {
    if(getConnectorId().isRootId()) {
      return "bucket";
    }
    return ConnectorCategory.super.getType();
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
    return true;
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }
  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }
}
