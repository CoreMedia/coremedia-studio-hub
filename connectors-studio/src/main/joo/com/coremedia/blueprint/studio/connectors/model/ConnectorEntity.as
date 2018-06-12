package com.coremedia.blueprint.studio.connectors.model {
public interface ConnectorEntity extends ConnectorObject {

  function getParent():ConnectorCategory;

  function getManagementUrl():String;

  function getThumbnailUrl():String;

  function getConnectionId():String;

  function isDeleteable():Boolean;

  function getContext():ConnectorContext;

  function getColumnValues():Array;

  function getRootCategory():ConnectorCategory;

  /**
   * Deletes this entity
   * @param callback optional callback called with the refreshed parent entity.
   * @param errorHandler optional callback called when an error has occured
   */
  function deleteEntity(callback:Function = null, errorHandler:Function = null):void;

  /**
   * Creates the preview HTML and metadata for this item
   * @param callback with preview HTML and metadata object
   */
  function preview(callback:Function):void;
}
}