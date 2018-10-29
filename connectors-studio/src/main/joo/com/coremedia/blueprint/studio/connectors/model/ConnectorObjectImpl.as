package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.impl.RemoteBeanImpl;

public class ConnectorObjectImpl extends RemoteBeanImpl implements ConnectorObject {
  public function ConnectorObjectImpl(uri:String) {
    super(uri);
  }

  override public function get(property:*):* {
    try {
      return super.get(property);
    } catch (e:Error) {
      // catalog objects such as marketing spots do not use stable IDs and may vanish any time :(
      trace("[INFO] ignoring error while accessing property", property, e);
      return null;
    }
  }

  public function getDisplayName():String {
    var name:String = get(ConnectorPropertyNames.DISPLAY_NAME);
    if(!name) {
      return getName();
    }
    return name;
  }

  public function getConnectorId():String {
    return get(ConnectorPropertyNames.CONNECTOR_ID).id;
  }

  public function getName():String {
    return get(ConnectorPropertyNames.NAME);
  }

  public function getConnector():Connector {
    return get(ConnectorPropertyNames.CONNECTOR);
  }

  public function getTypeLabel():String {
    return null;
  }

  public function getTypeCls():String {
    return null;
  }

  public function getTextCls():String {
    return null;//"connector-style-disabled connector-style-bold connector-style-line-through";//TODO not mapped yet
  }
}
}
