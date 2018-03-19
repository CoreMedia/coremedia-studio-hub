package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

[RestResource(uriTemplate="connector/category/{externalId:.+}")]
public class ConnectorCategoryImpl extends ConnectorEntityImpl implements ConnectorCategory {
  public function ConnectorCategoryImpl(uri:String, vars:Object) {
    super(uri);
    // set immediate vars
    setImmediateProperty(ConnectorPropertyNames.EXTERNAL_ID, vars.externalId);
  }

  public function getChildren():Array {
    return get(ConnectorPropertyNames.CHILDREN);
  }

  public function getSubCategories():Array {
    return get(ConnectorPropertyNames.SUB_CATEGORIES);
  }

  public function getItems():Array {
    return get(ConnectorPropertyNames.ITEMS);
  }

  public function getChildrenByName():Object {
    return get(ConnectorPropertyNames.CHILDREN_BY_NAME);
  }

  private function getRefreshUri():String {
    return get(ConnectorPropertyNames.REFRESH_URI);
  }

  public function isWriteable():Boolean {
    return get(ConnectorPropertyNames.WRITEABLE);
  }

  public function getType():String {
    return get(ConnectorPropertyNames.TYPE);
  }

  public function refresh(callback:Function = null):void {
    var method:RemoteServiceMethod = new RemoteServiceMethod(getRefreshUri(), 'GET');
    method.request({},
            function (response:RemoteServiceMethodResponse):void {
              invalidate(callback);
            },
            function (response:RemoteServiceMethodResponse):void {
              invalidate(callback);
            }
    );
  }

  public function getUploadUri():String {
    return get(ConnectorPropertyNames.UPLOAD_URI);
  }
}
}
