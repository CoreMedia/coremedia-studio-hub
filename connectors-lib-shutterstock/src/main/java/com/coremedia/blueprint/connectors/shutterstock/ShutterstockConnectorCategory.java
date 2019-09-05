package com.coremedia.blueprint.connectors.shutterstock;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.shutterstock.rest.Category;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShutterstockConnectorCategory extends ShutterstockConnectorEntity implements ConnectorCategory {
  private Category category;
  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();

  ShutterstockConnectorCategory(ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, Category category, String name) {
    super(parent, context, connectorId);
    this.category = category;
    setName(name);
  }

  @Override
  public String getType() {
    if(!getConnectorId().isRootId()) {
      return "shutterstockcategory";
    }
    return "shutterstock";
  }

  @Override
  public Date getLastModified() {
    return null;
  }

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

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    return items;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  public void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  public void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return true;
  }
}
