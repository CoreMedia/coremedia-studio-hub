package com.coremedia.blueprint.studio.connectors.model {
public class ConnectorContext {

  private var values:Object;

  public function ConnectorContext(values:Object) {
    this.values = values;
  }

  public function isMarkAsReadEnabled():Boolean {
    return values.markAsRead;
  }
}
}