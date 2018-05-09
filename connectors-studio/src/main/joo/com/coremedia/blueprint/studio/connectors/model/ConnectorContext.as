package com.coremedia.blueprint.studio.connectors.model {
public class ConnectorContext {

  private var values:Object;

  public function ConnectorContext(values:Object) {
    this.values = values;
  }

  public function isMarkAsReadEnabled():Boolean {
    return values.markAsRead;
  }

  public function getContentScope():String {
    return values.contentScope;
  }

  public function getDateFormat():String {
    return values.dateFormat || "long";
  }
}
}