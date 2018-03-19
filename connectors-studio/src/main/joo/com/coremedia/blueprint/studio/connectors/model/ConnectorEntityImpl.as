package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

public class ConnectorEntityImpl extends ConnectorObjectImpl implements ConnectorEntity{
  public function ConnectorEntityImpl(uri:String) {
    super(uri);
  }

  public function getManagementUrl():String {
    return get(ConnectorPropertyNames.MANAGEMENT_URL);
  }

  public function getParent():ConnectorCategory {
    return get(ConnectorPropertyNames.PARENT);
  }

  public function isDeleteable():Boolean {
    return get(ConnectorPropertyNames.DELETEABLE);
  }

  private function getDeleteUri():String {
    return get(ConnectorPropertyNames.DELETE_URI);
  }

  public function getContext():ConnectorContext {
    var connection:Connection = getConnector().getConnection(getConnectionId());
    return connection.getContext();
  }

  public function getConnectionId():String {
    return get(ConnectorPropertyNames.CONNECTOR_ID).connectionId;
  }

  public function deleteEntity(callback:Function = null, errorHandler:Function = null):void {
    var parent:ConnectorCategory = getParent() as ConnectorCategory;
    var method:RemoteServiceMethod = new RemoteServiceMethod(getDeleteUri(), 'GET');
    method.request({},
            function (response:RemoteServiceMethodResponse):void {
              var result:String = response.response.responseText;
              if(result && result === "false") {
                errorHandler(response.response.responseText);
              }
              if(parent) {
                parent.refresh(callback);
              }

            },
            function (response:RemoteServiceMethodResponse):void {
              if(parent) {
                parent.refresh(callback);
              }
            }
    );
  }
}
}
