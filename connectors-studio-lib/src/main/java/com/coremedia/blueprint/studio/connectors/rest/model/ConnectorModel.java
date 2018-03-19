package com.coremedia.blueprint.studio.connectors.rest.model;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContentMappings;
import com.coremedia.blueprint.connectors.api.ConnectorItemTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A single connector representation for REST
 */
public class ConnectorModel {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorModel.class);

  private String connectorType;
  private String name;
  private String siteId;
  private ConnectorItemTypes itemTypes;
  private ConnectorContentMappings contentMappings;

  private List<ConnectorConnectionModel> connections = new ArrayList<>();

  public ConnectorModel(String type, List<ConnectorConnection> cons) {
    this.connectorType = type;

    for (ConnectorConnection con : cons) {
      try {
        name = con.getContext().getProperty("name");
        itemTypes = con.getContext().getItemTypes();
        contentMappings = con.getContext().getContentMappings();
        connections.add(new ConnectorConnectionModel(con));
      } catch (Exception e) {
        LOGGER.error("Failed to create connector model for connections of type " + type + ":" + e.getMessage(), e);
      }
    }
  }

  public List<String> getItemTypes() {
    if(itemTypes != null) {
      return new ArrayList<>(itemTypes.getTypes().keySet());
    }
    return null;
  }

  public List<ConnectorConnectionModel> getConnections() {
    return connections;
  }

  public String getConnectorType() {
    return connectorType;
  }

  public String getName() {
    return name;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public ConnectorContentMappings getContentMappings() {
    return contentMappings;
  }
}
