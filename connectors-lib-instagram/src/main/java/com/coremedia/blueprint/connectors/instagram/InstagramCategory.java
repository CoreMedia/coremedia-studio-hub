package com.coremedia.blueprint.connectors.instagram;

import com.coremedia.connectors.library.DefaultConnectorColumn;
import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorColumn;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Category class for Instagram items
 */
public class InstagramCategory implements ConnectorCategory {

  public static final String CREATED_TIME = "createdTime";
  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private InstagramCategory parent;
  private List<ConnectorCategory> childCategories;
  private List<ConnectorItem> childItems;

  public InstagramCategory(ConnectorId id,
                            ConnectorContext context,
                            String name,
                            InstagramCategory parent,
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
  public List<ConnectorColumn> getColumns() {
    return Arrays.asList(new DefaultConnectorColumn(CREATED_TIME, CREATED_TIME, 120, 2));
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
