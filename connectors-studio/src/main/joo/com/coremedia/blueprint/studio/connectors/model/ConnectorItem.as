package com.coremedia.blueprint.studio.connectors.model {
public interface ConnectorItem extends ConnectorEntity {

  function getItemType():String;

  function getOpenInTabUrl():String;

  function getDownloadUrl():String;

  function getStreamUrl():String;

  function getMetadata():Object;

  function getStatus():String;

  function getSize():Number;

  function isDownloadable():Boolean;

  function download():void;

  function openInTab():void;

  /**
   * Creates the preview HTML and metadata for this item
   * @param callback with preview HTML and metadata object
   */
  function preview(callback:Function):void;
}
}
