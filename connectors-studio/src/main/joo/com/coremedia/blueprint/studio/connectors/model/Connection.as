package com.coremedia.blueprint.studio.connectors.model {
public class Connection {
  private var values:Object;
  private var context:ConnectorContext;

  public function Connection(values:Object) {
    this.values = values;
    this.context = new ConnectorContext(values.context);
  }

  public function getContext():ConnectorContext {
    return context;
  }
}
}