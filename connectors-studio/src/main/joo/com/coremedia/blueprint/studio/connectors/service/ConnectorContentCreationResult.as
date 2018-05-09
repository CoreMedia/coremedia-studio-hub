package com.coremedia.blueprint.studio.connectors.service {
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.cap.content.Content;

public class ConnectorContentCreationResult {
  public var content:Content;
  public var connectorEntity:ConnectorEntity;

  public function ConnectorContentCreationResult(content:Content, entity:ConnectorEntity) {
    this.content = content;
    this.connectorEntity = entity;
  }
}
}