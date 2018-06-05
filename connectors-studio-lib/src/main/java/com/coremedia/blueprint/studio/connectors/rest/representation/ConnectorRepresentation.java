package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContentMappings;
import com.coremedia.blueprint.studio.connectors.rest.model.ConnectorConnectionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectorRepresentation {
  private String name;
  private String siteId;
  private String connectorType;
  private List<ConnectorConnectionModel> connections;
  private List<String> itemTypes;
  private Map<String, String> contentMappings = new HashMap<>();

  public List<String> getItemTypes() {
    return itemTypes;
  }

  public void setItemTypes(List<String> itemTypes) {
    this.itemTypes = itemTypes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ConnectorConnectionModel> getConnections() {
    return connections;
  }

  public void setConnections(List<ConnectorConnectionModel> connections) {
    this.connections = connections;
  }

  public Map<String, ConnectorChildRepresentation> getChildrenByName() {
    Map<String, ConnectorChildRepresentation> result = new LinkedHashMap<>();
    for (ConnectorConnectionModel connection : connections) {
      ConnectorCategory root = connection.getRootCategory();
      result.put(root.getConnectorId().getId(), new ConnectorChildRepresentation(root.getDisplayName(), root));
    }
    return result;
  }

  public List<ConnectorCategory> getRootCategories() {
    List<ConnectorCategory> result = new ArrayList<>();
    for (ConnectorConnectionModel connection : connections) {
      ConnectorCategory root = connection.getRootCategory();
      result.add(root);
    }
    return result;
  }

  public String getConnectorType() {
    return connectorType;
  }

  public void setConnectorType(String connectorType) {
    this.connectorType = connectorType;
  }

  public Map<String, String> getContentMappings() {
    return contentMappings;
  }

  public void setContentMappings(ConnectorContentMappings mappings) {
    if (mappings != null) {
      Set<Map.Entry<String, Object>> entries = mappings.getProperties().entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        Object value = entry.getValue();
        this.contentMappings.put(entry.getKey(), String.valueOf(value));
      }
    }
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }
}
