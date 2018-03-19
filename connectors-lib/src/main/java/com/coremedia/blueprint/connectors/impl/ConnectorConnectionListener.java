package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;

/**
 * Listener that can be used to listen to changed on connector connections
 */
public interface ConnectorConnectionListener {

  /**
   * Called when a new connection is initialized for a connector.
   * @param connection the connection of the connector
   */
  void initialized(ConnectorConnection connection);

  /**
   * Called when the connection is terminated, e.g when the configuration has changed
   * and the connection is re-initialized.
   * @param connection the connection of the connector
   */
  void terminated(ConnectorConnection connection);
}
