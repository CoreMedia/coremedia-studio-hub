package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cms.editor.sdk.util.ContentLocalizationUtil;
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

  override public function getTypeLabel():String {
    var itemType:String = getItemType();
    var connectorType:String = getConnector().getConnectorType();
    if (itemType) {
      if(connectorType.indexOf(ConnectorHelper.TYPE_COREMEDIA_CONNECTOR) !== -1) {
        return ContentLocalizationUtil.getLabelForContentType(SESSION.getConnection().getContentRepository().getContentType(itemType));
      }

      if (itemType.indexOf('/') !== -1) {
        itemType = itemType.substr(itemType.indexOf('/') + 1, itemType.length);
      }
      var itemLabel:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'item_type_' + itemType + '_name');
      if (itemLabel) {
        return itemLabel;
      }

      return ConnectorHelper.camelizeWithWhitespace(itemType);
    }
  }


  override public function getTypeCls():String {
    var connectorType:String = getConnector().getConnectorType();
    var itemType:String = getItemType();

    if(connectorType.indexOf(ConnectorHelper.TYPE_COREMEDIA_CONNECTOR) !== -1) {
      return ContentLocalizationUtil.getIconStyleClassForContentType(SESSION.getConnection().getContentRepository().getContentType(itemType));
    }

    if (itemType.indexOf('/') !== -1) {
      itemType = itemType.substr(itemType.indexOf('/') + 1, itemType.length);
    }

    var icon:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "item_type_" + itemType + "_icon");
    if (icon) {
      return icon;
    }

    var name:String = getName();
    var suffix:String = null;
    if (name.indexOf(".") !== -1) {
      suffix = name.substr(name.lastIndexOf(".") + 1, name.length);
      if (suffix.length === 4) {
        suffix = suffix.substr(0, 3);
      }

      icon = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "item_type_" + suffix + "_icon");
      if (icon) {
        return icon;
      }
    }
    return ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Item_icon');
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
