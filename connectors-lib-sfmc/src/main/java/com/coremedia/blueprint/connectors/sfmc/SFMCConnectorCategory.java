package com.coremedia.blueprint.connectors.sfmc;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCCategory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SFMCConnectorCategory extends SFMCConnectorEntity implements ConnectorCategory {

  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();
  private SFMCCategory category;

  SFMCConnectorCategory(SFMCConnectorServiceImpl service, ConnectorCategory parent, SFMCCategory category, ConnectorContext context, ConnectorId id) {
    super(service, parent, category, context, id);
    this.category = category;
  }

  @Override
  public String getType() {
    if(getConnectorId().isRootId()) {
      return "sfmc";
    }
    return ConnectorCategory.super.getType();
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
    return true;
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return service.refresh(context, this);
  }

  @Override
  public ConnectorItem upload(@NonNull ConnectorContext context, String itemName, InputStream inputStream) {
    return null;
  }
}
