package com.coremedia.blueprint.studio.connectors.model {
import mx.resources.ResourceManager;

[RestResource(uriTemplate="connector/connector/{connectorType:[^/]+}/{siteId:[^/]+}")]
public class ConnectorImpl extends ConnectorObjectImpl implements Connector {

  public function ConnectorImpl(uri:String, vars:Object) {
    super(uri);
    setImmediateProperty(ConnectorPropertyNames.SITE_ID, vars.siteId);
    setImmediateProperty(ConnectorPropertyNames.CONNECTOR_TYPE, vars.connectorType);
  }

  public function getChildrenByName():Object {
    return get(ConnectorPropertyNames.CHILDREN_BY_NAME);
  }

  override public function getConnector():Connector {
    return this;
  }

  public function getConnectorType():String {
    return get(ConnectorPropertyNames.CONNECTOR_TYPE);
  }

  public function getItemTypes():Array {
    return get(ConnectorPropertyNames.ITEM_TYPES);
  }

  public function getRootCategories():Array {
    return get(ConnectorPropertyNames.ROOT_CATEGORIES);
  }

  public function getConnection(connectionId:String):Connection {
    var connections:Array = get(ConnectorPropertyNames.CONNECTIONS);
    if(connections) {
      for each(var connection:Object in connections) {
        if(connection.connectionId === connectionId) {
          return new Connection(connection);
        }
      }
    }
    return null;
  }

  override public function getTypeLabel():String {
    var connectorLabel:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'connector_type_' + getConnectorType() + "_name");
    if (!connectorLabel) {
      connectorLabel = getConnectorType();
    }
    return connectorLabel;
  }

  override public function getTypeCls():String {
    var connectorType:String = getConnectorType();
    var icon:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "connector_type_" + connectorType + "_icon");
    if (!icon) {
      icon = ResourceManager.getInstance().getString('com.coremedia.icons.CoreIcons', 'folder_open');
    }
    return icon;
  }
}
}
