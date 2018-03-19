package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.RemoteBean;

public interface ConnectorObject extends RemoteBean {

  function getName():String;

  function getConnector():Connector;

  function getDisplayName():String;

  function getConnectorId():String;
}
}
