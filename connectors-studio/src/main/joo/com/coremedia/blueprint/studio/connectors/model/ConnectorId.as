package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.beanFactory;

public class ConnectorId {
  private const PARENT_ID_SEPARATOR:String = "|";
  private var connectionId:String;
  private var externalId:String;
  private var id:String;

  public function ConnectorId(id:String):void {
    var segment:String = id;
    for (var i:int = 0; i < 5; i++) {
      segment = segment.substring(segment.indexOf("/") + 1, segment.length);
    }

    connectionId = id.split("/")[3];
    this.id = id;
    this.connectionId = connectionId;
    this.externalId = segment;
  }

  public function getConnectionId():String {
    return connectionId;
  }

  public function getExternalId():String {
    if(externalId.indexOf(PARENT_ID_SEPARATOR) ===-1) {
      return externalId;
    }
    return externalId.split(PARENT_ID_SEPARATOR)[0];
  }

  public function getParentId():ConnectorId {
    if(externalId.indexOf(PARENT_ID_SEPARATOR) ===-1) {
      var parentId:String = externalId.split(PARENT_ID_SEPARATOR)[1];
      return new ConnectorId(parentId);
    }
    return null;
  }

  public function toString():String {
    return id;
  }

  public function toUrl() {
    return encodeURIComponent(encodeURIComponent(id));
  }

  public function toConnectorEntity():ConnectorEntity {
    if(id.indexOf('/item/') !== -1) {
      var itemUriPath:String = "connector/item/" + toUrl();
      return beanFactory.getRemoteBean(itemUriPath) as ConnectorItem;
    }
    else if(id.indexOf('/category/') !== -1) {
      var categoryUriPath:String = "connector/category/" + toUrl();
      return beanFactory.getRemoteBean(categoryUriPath) as ConnectorCategory;
    }

    throw new Error("Unsupported connector id '" + id + "'");
  }
}
}