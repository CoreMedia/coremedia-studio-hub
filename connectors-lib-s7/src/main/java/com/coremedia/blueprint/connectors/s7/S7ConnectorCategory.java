package com.coremedia.blueprint.connectors.s7;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumn;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class S7ConnectorCategory extends S7ConnectorEntity implements ConnectorCategory {

  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();

  S7ConnectorCategory(ConnectorCategory parent, ConnectorContext context, ConnectorId id, S7Container file) {
    super(parent, context, id, file);
  }

    /*@NonNull
    @Override
    public List<ConnectorEntity> getChildren() {
        ArrayList<ConnectorEntity> children = new ArrayList<>();
        children.addAll(getSubCategories());
        children.addAll(getItems());
        return children;
    }*/

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    return items;
  }

  @NonNull
  @Override
  public List<ConnectorColumn> getColumns() {
    return null;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }
}
