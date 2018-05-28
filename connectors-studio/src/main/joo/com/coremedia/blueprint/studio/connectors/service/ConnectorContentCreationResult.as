package com.coremedia.blueprint.studio.connectors.service {
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.cap.content.Content;

public class ConnectorContentCreationResult {
  public var content:Content;
  public var connectorEntity:ConnectorEntity;
  public var isNew:Boolean = false;

  public function ConnectorContentCreationResult(content:Content, entity:ConnectorEntity, isNew:Boolean) {
    this.content = content;
    this.connectorEntity = entity;
    this.isNew = isNew;
  }
}
}