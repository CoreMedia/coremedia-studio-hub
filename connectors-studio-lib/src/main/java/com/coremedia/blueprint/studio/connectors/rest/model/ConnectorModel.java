package com.coremedia.blueprint.studio.connectors.rest.model;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorItemTypes;
import com.coremedia.cap.multisite.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A single connector representation for REST
 */
public class ConnectorModel {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorModel.class);

  private String connectorType;
  private String name;
  private Locale locale;
  private Site site;
  private ConnectorItemTypes itemTypes;

  private List<ConnectorConnectionModel> connections = new ArrayList<>();

  public ConnectorModel(String type, Site site, Locale locale, List<ConnectorConnection> cons) {
    this.connectorType = type;
    this.locale = locale;
    this.site = site;

    for (ConnectorConnection con : cons) {
      try {
        name = con.getContext().getTypeName();
        itemTypes = con.getContext().getItemTypes();
        connections.add(new ConnectorConnectionModel(con));
      } catch (ConnectorException e) {
        LOGGER.warn("Failed to initialize model for connections of type " + type + ": " + e.getMessage());
      } catch (Exception e) {
        LOGGER.error("Failed to create connector model for connections of type " + type + ":" + e.getMessage(), e);
      }
    }
  }

  public List<String> getItemTypes() {
    if (itemTypes != null) {
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
    if(site != null) {
      return site.getId();
    }
    return null;
  }

  public Locale getLocale() {
    return locale;
  }
}
