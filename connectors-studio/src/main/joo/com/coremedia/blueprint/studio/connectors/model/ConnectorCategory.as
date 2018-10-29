package com.coremedia.blueprint.studio.connectors.model {
public interface ConnectorCategory extends ConnectorEntity {

  function getChildren():Array;

  function getSubCategories():Array;

  function getItems():Array;

  function getChildrenByName():Object;

  function isWriteable():Boolean;

  function isContentUploadEnabled():Boolean;

  function dropContents(contents:Array, defaultAction:Boolean, callback:Function = undefined):void;

  function getColumns():Array;

  /**
   * Refreshes the given category
   * @param callback the callback invoked with the invalidated category when refresh is finished
   */
  function refresh(callback:Function = undefined):void;

  function getUploadUri():String;

  function getType():String;
}
}
