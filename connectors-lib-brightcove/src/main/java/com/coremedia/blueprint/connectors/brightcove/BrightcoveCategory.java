package com.coremedia.blueprint.connectors.brightcove;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Date;
import java.util.List;

/**
 *
 */
public class BrightcoveCategory implements ConnectorCategory {

  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private BrightcoveCategory parent;
  private List<ConnectorCategory> childCategories;
  private List<ConnectorItem> childItems;

  public BrightcoveCategory(ConnectorId id,
                            ConnectorContext context,
                            String name,
                            BrightcoveCategory parent,
                            List<ConnectorCategory> childCategories,
                            List<ConnectorItem> childItems) {
    this.id = id;
    this.context = context;
    this.name = name;
    this.parent = parent;
    this.childCategories = childCategories;
    this.childItems = childItems;
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return childCategories;
  }

  @NonNull
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

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  @NonNull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @NonNull
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
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  void setItems(List<ConnectorItem> items) {
    this.childItems = items;
  }
}
