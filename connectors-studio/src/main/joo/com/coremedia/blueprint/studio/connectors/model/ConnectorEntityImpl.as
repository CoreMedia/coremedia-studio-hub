package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

public class ConnectorEntityImpl extends ConnectorObjectImpl implements ConnectorEntity {
  public function ConnectorEntityImpl(uri:String) {
    super(uri);
  }

  public function getManagementUrl():String {
    return get(ConnectorPropertyNames.MANAGEMENT_URL);
  }

  public function getThumbnailUrl():String {
    return get(ConnectorPropertyNames.THUMBNAIL_URL);
  }

  public function getParent():ConnectorCategory {
    return get(ConnectorPropertyNames.PARENT);
  }

  public function isDeleteable():Boolean {
    return get(ConnectorPropertyNames.DELETEABLE);
  }

  public function getColumnValues():Array {
    return get(ConnectorPropertyNames.COLUMN_VALUES);
  }

  public function getPreviewUri():String {
    return get(ConnectorPropertyNames.PREVIEW_URI);
  }

  public function getRootCategory():ConnectorCategory {
    var myConnectorId:ConnectorId = new ConnectorId(getConnectorId());
    var connectionId:String = myConnectorId.getConnectionId();
    var rootCategoryId:ConnectorId = ConnectorId.createRootId(connectionId);

    var encodedId:String = encodeURIComponent(rootCategoryId.toString());
    encodedId = encodeURIComponent(encodedId);
    var uriPath:String = 'connector/category/' + encodedId;
    var bean:ConnectorCategory = beanFactory.getRemoteBean(uriPath) as ConnectorCategory;
    bean.load();
    return bean;
  }

  public function getColumnValue(dataIndex:String):Object {
    if (!isLoaded()) {
      return undefined;
    }

    if (!getColumnValues()) {
      return null;
    }

    for each(var value:Object in getColumnValues()) {
      if (value.dataIndex === dataIndex) {
        return value;
      }
    }
    return null;
  }

  public function getContext():ConnectorContext {
    if (!this.getConnector().isLoaded()) {
      this.getConnector().load();
      return undefined;
    }
    var connection:Connection = getConnector().getConnection(getConnectionId());
    if (connection) {
      return connection.getContext();
    }
    return null;
  }

  public function getConnectionId():String {
    return get(ConnectorPropertyNames.CONNECTOR_ID).connectionId;
  }

  public function preview(callback:Function):void {
    //must be implemented by children
  }

  public function deleteEntity(callback:Function = null, errorHandler:Function = null):void {
    var parent:ConnectorCategory = getParent() as ConnectorCategory;
    var method:RemoteServiceMethod = new RemoteServiceMethod(getUriPath(), 'DELETE');
    var entity:ConnectorEntity = this;
    method.request({},
            function (response:RemoteServiceMethodResponse):void {
              var result:String = response.response.responseText;
              if (result && result === "false") {
                errorHandler(response.response.responseText);
              }
              if (parent) {
                parent.refresh(callback);
              }
              else {
                trace('[WARN]', 'No parent category found for ' + entity + '. Ensure that the parent is set to ensure that a refresh is applied after deletion.');
              }

            },
            function (response:RemoteServiceMethodResponse):void {
              if (parent) {
                parent.refresh(callback);
              }
            }
    );
  }


  override public function toString():String {
    return "Connector Entity (" + getUriPath() + ")";
  }
}
}
