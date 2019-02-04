package com.coremedia.blueprint.studio.connectors.model {
public interface ConnectorCategory extends ConnectorEntity {

  function getChildren():Array;

  function getSubCategories():Array;

  function getItems():Array;

  function getChildrenByName():Object;

  function isWriteable():Boolean;

  function isContentUploadEnabled():Boolean;

  /**
   * Uploads the given array of contents
   * @param contents the contents to upload
   * @param propertyNames the property names that should be uploaded. If null is set, the default mapping will be used.
   * @param defaultAction true, if the default action should be used
   * @param callback the optional callback to call after upload is completed.
   */
  function uploadContents(contents:Array, propertyNames:Array = undefined, defaultAction:Boolean = undefined, callback:Function = undefined):void;

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
