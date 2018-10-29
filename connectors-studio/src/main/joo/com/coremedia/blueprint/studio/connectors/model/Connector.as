package com.coremedia.blueprint.studio.connectors.model {
public interface Connector extends ConnectorObject {
  function getChildrenByName():Object;

  function getRootCategories():Array;

  function getConnectorType():String;

  function getItemTypes():Array;

  function getConnection(connectionId:String):Connection;
}
}
