package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.blueprint.studio.connectors.rest.model.ConnectorModel;

import java.util.List;

public class ConnectorsRepresentation {
  private List<ConnectorModel> connectors;

  public List<ConnectorModel> getConnectors() {
    return connectors;
  }

  public void setConnectors(List<ConnectorModel> connectors) {
    this.connectors = connectors;
  }
}
