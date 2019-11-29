package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumn;
import com.coremedia.blueprint.connectors.api.ConnectorColumnValue;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.celum.rest.Asset;
import com.coremedia.blueprint.connectors.celum.rest.Localization;
import com.coremedia.blueprint.connectors.celum.rest.Node;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumn;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class CelumConnectorCategory extends CelumConnectorEntity implements ConnectorCategory {

  private Node node;
  private String name;
  private ConnectorCategory parent;
  private List<ConnectorCategory> childCategories;
  private List<ConnectorItem> childItems = new ArrayList<>();
  private CelumConnectorService service;

  CelumConnectorCategory(CelumConnectorService service, ConnectorContext context, ConnectorId id, String name, List<ConnectorCategory> childCategories) {
    super(context, id);
    this.service = service;
    this.name = name;
    this.childCategories = childCategories;
  }

  CelumConnectorCategory(CelumConnectorService service, ConnectorContext context, ConnectorId id, Node node, ConnectorCategory parent, List<ConnectorCategory> childCategories) {
    super(context, id);
    this.service = service;
    this.node = node;
    this.parent = parent;
    this.childCategories = childCategories;
  }

  public Asset getAsset(int id) {
    return node.getAsset(id);
  }

  public List<Asset> getAssets() {
    if(node != null) {
      return node.getAssets();
    }
    return Collections.emptyList();
  }

  @Override
  public String getType() {
    if(node != null) {
      String type = node.getType().getLabel(Locale.getDefault().getLanguage());
      if(type.toLowerCase().equals("keyword")) {
        return "tag";
      }
    }
    return ConnectorCategory.super.getType();
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return childCategories;
  }

  @NonNull
  @Override
  public List<ConnectorColumn> getColumns() {
    DefaultConnectorColumn col1 = new DefaultConnectorColumn("folder_header", DATA_INDEX_CELUM_FOLDER, 150);
    DefaultConnectorColumn col2 = new DefaultConnectorColumn("id_header", DATA_INDEX_CELUM_ID, 50);
    return Arrays.asList(col1, col2);
  }

  @Override
  public List<ConnectorColumnValue> getColumnValues() {
    if(node != null) {
      DefaultConnectorColumnValue v1 = new DefaultConnectorColumnValue(String.valueOf(node.getId()), DATA_INDEX_CELUM_ID);

      return Arrays.asList(v1);
    }
    return Arrays.asList(new DefaultConnectorColumnValue("", DATA_INDEX_CELUM_ID));
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

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();

    if(node != null) {
      data.put("Celum Id", node.getId());
      data.put("Type", node.getType().getLabel(Locale.getDefault().getLanguage()));

      Date creationDate = node.getModificationInformation().getCreationDate();
      data.put("creationDate", Localization.formatDate(Locale.getDefault(), creationDate));
    }

    return () -> data;
  }

  @NonNull
  @Override
  public String getName() {
    if(name != null) {
      return name;
    }
    String lang = Locale.getDefault().getLanguage();
    return node.getLabel(lang);
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
    if(node != null) {
      return node.getModificationInformation().getLastModificationDate();
    }
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return context.getProperty("managementUrl");
  }

  @Override
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  public void setChildItems(List<ConnectorItem> childItems) {
    this.childItems = childItems;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return service.refresh(context);
  }
}
