package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

import ext.JSON;
import ext.StringUtil;

import mx.resources.ResourceManager;

[RestResource(uriTemplate="connector/item/{externalId:.+}")]
public class ConnectorItemImpl extends ConnectorEntityImpl implements ConnectorItem {
  public function ConnectorItemImpl(uri:String, vars:Object) {
    super(uri);
    // set immediate vars
    setImmediateProperty(ConnectorPropertyNames.EXTERNAL_ID, vars.externalId);
  }

  public function getItemType():String {
    return get(ConnectorPropertyNames.ITEM_TYPE);
  }

  public function getOpenInTabUrl():String {
    return get(ConnectorPropertyNames.OPEN_IN_TAB_URL);
  }

  public function getSize():Number {
    return get(ConnectorPropertyNames.SIZE);
  }

  public function getMetadata():Object {
    return get(ConnectorPropertyNames.META_DATA);
  }

  public function isDownloadable():Boolean {
    return get(ConnectorPropertyNames.DOWNLOADABLE);
  }

  public function getTargetContentType():String {
    return get(ConnectorPropertyNames.TARGET_CONTENT_TYPE);
  }

  public function download():void {
    var url:String = getDownloadUrl();
    window.open(url, "_blank");
  }

  public function openInTab():void {
    var url:String = getOpenInTabUrl();
    window.open(url, "_blank");
  }

  override public function preview(callback:Function):void {
    var method:RemoteServiceMethod = new RemoteServiceMethod(getPreviewUri(), 'GET');
    method.request(
            {},
            function (response:RemoteServiceMethodResponse):void {
              var result:String = response.response.responseText;
              if (result) {
                var previewRepresentation:Object = JSON.decode(result);
                var html:String = previewRepresentation.html;
                if (html) {
                  var url:String = getStreamUrl();

                  //switch data URL based on the preview templates, e.g. PDF documents must be downloaded
                  if(html.indexOf('embed') !== -1 || html.indexOf('iframe') !== -1) {
                    url = getOpenInTabUrl();
                  }

                  if(getName().indexOf(".svg") !== -1) {
                    html = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'preview_not_supported_svg');
                  }

                  //complex JS may break the format operation, since we have text here, it isn't required anyway
                  if(html.indexOf("<textarea") !== 0) {
                    html = StringUtil.format(html, url);
                  }
                }

              }
              callback(html, previewRepresentation[ConnectorPropertyNames.META_DATA]);
            },
            function (response:RemoteServiceMethodResponse):void {
              callback(response.getError());
            }
    );
  }

  public function getStreamUrl():String {
    return get(ConnectorPropertyNames.STREAM_URL);
  }

  public function getDownloadUrl():String {
    return get(ConnectorPropertyNames.DOWNLOAD_URL);
  }
}
}
