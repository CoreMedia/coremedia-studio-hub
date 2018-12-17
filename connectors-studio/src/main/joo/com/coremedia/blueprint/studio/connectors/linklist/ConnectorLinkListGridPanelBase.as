package com.coremedia.blueprint.studio.connectors.linklist {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.blueprint.studio.connectors.model.ConnectorPropertyNames;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.fields.LinkListGridPanel;
import com.coremedia.cms.editor.sdk.util.ILinkListWrapper;
import com.coremedia.cms.editor.sdk.util.ImageLinkListRenderer;
import com.coremedia.cms.editor.sdk.util.PropertyEditorUtil;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.store.BeanRecord;

import mx.resources.ResourceManager;

public class ConnectorLinkListGridPanelBase extends LinkListGridPanel {

  private var content:*;
  private var _localWrapper:ILinkListWrapper;

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var model:Bean;

  [Bindable]
  public var propertyName:String;

  [Bindable]
  public var maxCardinality:Number;

  [Bindable]
  public var createStructFunction:Function;

  [Bindable]
  public var linkTypeNames:Array;

  [Bindable]
  public var forceReadOnlyValueExpression:ValueExpression;

  public function ConnectorLinkListGridPanelBase(config:ConnectorLinkListGridPanel = null) {
    super(config);
    bindTo = config.bindTo;
  }

  protected function getLinkListWrapper(config:ConnectorLinkListGridPanelBase):ILinkListWrapper {
    if (!_localWrapper) {
      if (config.linkListWrapper) {
        _localWrapper = config.linkListWrapper;
      } else {
        var wrapperCfg:ConnectorLinkListWrapper = ConnectorLinkListWrapper({});
        wrapperCfg.bindTo = config.bindTo;
        wrapperCfg.model = config.model;
        wrapperCfg.propertyName = config.propertyName;
        wrapperCfg.maxCardinality = config.maxCardinality;
        wrapperCfg.createStructFunction = config.createStructFunction;
        wrapperCfg.linkTypeNames = [ConnectorPropertyNames.TYPE_CONNECTOR_ENTITY];
        wrapperCfg.readOnlyVE = getReadOnlyVE(config);
        _localWrapper = new ConnectorLinkListWrapper(wrapperCfg);
      }
    }
    return _localWrapper;
  }

  protected function getReadOnlyVE(config:ConnectorLinkListGridPanelBase):ValueExpression {
    if (!readOnlyValueExpression) {
      if (config.readOnlyValueExpression) {
        readOnlyValueExpression = config.readOnlyValueExpression;
      } else {
        readOnlyValueExpression = PropertyEditorUtil.createReadOnlyValueExpression(config.bindTo, config.forceReadOnlyValueExpression);
      }

    }
    return readOnlyValueExpression;
  }

  [ProvideToExtChildren]
  internal function getContent():* {
    return content;
  }

  internal static function convertTypeLabel(v:String, connectorObject:ConnectorEntity):String {
    if(!connectorObject.isLoaded()) {
      return undefined;
    }

    if (connectorObject is ConnectorEntity) {
      return ConnectorHelper.getTypeLabel(connectorObject);
    }
  }

  internal static function convertTypeCls(v:String, connectorObject:ConnectorEntity):String {
    if(!connectorObject.isLoaded()) {
      return undefined;
    }

    if (connectorObject is ConnectorEntity) {
      return ConnectorHelper.getTypeCls(connectorObject);
    }
  }

  internal static function convertName(v:String, connectorObject:ConnectorEntity):String {
    if(!connectorObject.isLoaded()) {
      return undefined;
    }

    if (!connectorObject.getRootCategory().isLoaded()) {
      return undefined;
    }

    return connectorObject.getDisplayName();
  }

  protected function nameRenderer(value:Object, metaData:Object, record:BeanRecord):String {
    var connectorObject:ConnectorEntity = record.getBean() as ConnectorEntity;
    if(!connectorObject.isLoaded()) {
      return undefined;
    }

    if(!connectorObject.getRootCategory().isLoaded()) {
      return undefined;
    }

    return "<div><span>" + connectorObject.getDisplayName() + "</span><br><span style=\"font-size:11px;\">" + connectorObject.getRootCategory().getDisplayName() + "</span></div>";
  }

  public static function convertThumbnail(connectorObject:ConnectorEntity):String {
    if(!connectorObject.isLoaded()) {
      return undefined;
    }

    return editorContext.getThumbnailUri(connectorObject, null, ConnectorHelper.getType(connectorObject as ConnectorObject));
  }

  protected function thumbColRenderer(value:Object, metaData:Object, record:BeanRecord):String {
    var thumbUri:String = record.data.thumbnailUrl;
    if(thumbUri === undefined) {
      return undefined;
    }

    var entity:ConnectorEntity = record.getBean() as ConnectorEntity;
    if(!entity.isLoaded()) {
      entity.load();
      return undefined;
    }

    if(!entity.getConnector().isLoaded()) {
      entity.getConnector().load();
      return undefined;
    }

    return thumbnailFor(thumbUri, entity);
  }

  /**
   * Returns the thumbnail template for the given uri.
   * if uri is empty, delegates to {@link #emptyThumbnail}
   * @param uri the uri to generate the thumbnail template for
   */
  private static function thumbnailFor(uri:String, connectorEntity:ConnectorEntity):String {
    if (!uri) {
      var connectorType:String = ConnectorHelper.getTypeCls(connectorEntity.getConnector());
      return '<div class="cm-image-thumbnail">' +
              '<div style="box-sizing: content-box;\n' +
              '    text-align: center;\n' +
              '    font-size: 56px;\n' +
              '    height: 42px;\n' +
              '    line-height: 1;\n' +
              '    margin-top: 0px;" class="' + connectorType + '" title="' + connectorEntity.getDisplayName() + '"/></div>'
    }
    return '<div class="cm-thumbnail-image cm-thumbnail-image--link-list">' +
            '<img style="max-width:80px;max-height:54px;" class="cm-image-thumbnail__image" src="' + uri + '" /></div>'
  }

  protected function onDropAreaClick():void {
    editorContext.getCollectionViewManager().openRepository();
  }
}
}
