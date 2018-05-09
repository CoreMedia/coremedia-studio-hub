package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumn;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorItem;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class ConnectorCategoryRepresentation extends ConnectorEntityRepresentation {

  private URI refreshUri;
  private URI uploadUri;

  private List<ConnectorCategory> subCategories;
  private List<ConnectorItem> items;
  private List<ConnectorEntity> children;
  private List<ConnectorColumn> columns;
  private Map<String, ConnectorChildRepresentation> childrenByName;
  private boolean writeable;
  private String type;

  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  public List<ConnectorEntity> getChildren() {
    return children;
  }

  public void setChildren(List<ConnectorEntity> children) {
    this.children = children;
  }

  public Map<String, ConnectorChildRepresentation> getChildrenByName() {
    return childrenByName;
  }

  public void setChildrenByName(Map<String, ConnectorChildRepresentation> childrenByName) {
    this.childrenByName = childrenByName;
  }

  public URI getRefreshUri() {
    return refreshUri;
  }

  public void setRefreshUri(URI refreshUri) {
    this.refreshUri = refreshUri;
  }

  public boolean isWriteable() {
    return writeable;
  }

  public void setWriteable(boolean writeable) {
    this.writeable = writeable;
  }

  public URI getUploadUri() {
    return uploadUri;
  }

  public void setUploadUri(URI uploadUri) {
    this.uploadUri = uploadUri;
  }

  public List<ConnectorItem> getItems() {
    return items;
  }

  public void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<ConnectorColumn> getColumns() {
    return columns;
  }

  public void setColumns(List<ConnectorColumn> columns) {
    this.columns = columns;
  }
}
