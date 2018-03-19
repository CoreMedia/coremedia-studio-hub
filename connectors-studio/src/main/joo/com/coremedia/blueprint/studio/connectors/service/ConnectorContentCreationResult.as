package com.coremedia.blueprint.studio.connectors.service {
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.cap.content.Content;

public class ConnectorContentCreationResult {
  public var content:Content;
  public var connectorItem:ConnectorItem;

  public function ConnectorContentCreationResult(content:Content, item:ConnectorItem) {
    this.content = content;
    this.connectorItem = item;
  }
}
}