package com.coremedia.blueprint.studio.connectors.model {
public interface ConnectorItem extends ConnectorEntity {

  function getItemType():String;

  function getOpenInTabUrl():String;

  function getDownloadUrl():String;

  function getStreamUrl():String;

  function getMetadata():Object;

  function getSize():Number;

  function isDownloadable():Boolean;

  function download():void;

  function openInTab():void;

  function getTargetContentType():String;
}
}
