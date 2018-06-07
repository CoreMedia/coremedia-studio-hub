package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ExampleConnectorCategory implements ConnectorCategory {
  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private ExampleConnectorCategory parent;
  private List<ConnectorCategory> childCategories;
  private List<ConnectorItem> childItems;

  public ExampleConnectorCategory(ConnectorId id,
                                  ConnectorContext context,
                                  String name,
                                  ExampleConnectorCategory parent,
                                  List<ConnectorCategory> childCategories,
                                  List<ConnectorItem> childItems) {
    this.id = id;
    this.context = context;
    this.name = name;
    this.parent = parent;
    this.childCategories = childCategories;
    this.childItems = childItems;
  }

  @Nonnull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return childCategories;
  }

  @Nonnull
  @Override
  public List<ConnectorItem> getItems() {
    return childItems;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
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
}
