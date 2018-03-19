package com.coremedia.blueprint.studio.connectors.library.preview {
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.skins.LoadMaskSkin;

import ext.LoadMask;
import ext.form.field.DisplayField;
import ext.panel.Panel;

public class ItemPreviewPanelBase extends Panel {
  private var activePreviewExpression:ValueExpression;
  private var loadMask:LoadMask;
  private const videoDelayMillis:Number = 500;
  private const videoPreviewTimeout:Number = 10000;

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  public var selectedItemValueExpression:ValueExpression;
  public var metadataValueExpression:ValueExpression;

  public function ItemPreviewPanelBase(config:ItemPreviewPanel = null) {
    super(config);
  }

  override protected function afterRender():void {
    super.afterRender();

    var loadMaskCfg:LoadMask = LoadMask({
      target: this,
      ui: LoadMaskSkin.OPAQUE.getSkin()
    });
    loadMaskCfg.msg = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'preview_loading');
    loadMask = new LoadMask(loadMaskCfg);
    loadMask.disable();
    loadMask.hide();

    selectedItemsValueExpression.addChangeListener(selectionChanged);
  }

  protected function getSelectedItemExpression():ValueExpression {
    if (!selectedItemValueExpression) {
      selectedItemValueExpression = ValueExpressionFactory.createFromValue(null);
    }
    return selectedItemValueExpression;
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selection:ConnectorObject = ve.getValue()[0];
    getSelectedItemExpression().setValue(selection);
    getDisplayField().setValue('<i>' + resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'preview_loading') + '...</i>');

    var previewItemId:String = resolvePreviewPanel(selection);
    getActivePreviewExpression().setValue(previewItemId);

    var value:ConnectorObject = ve.getValue()[0];
    if (value is ConnectorItem) {
      var item:ConnectorItem = value as ConnectorItem;
      var itemType:String = item.getItemType();
      if (itemType) {
        loadMask.setVisible(true);

        var dom:* = queryById('connectorPreviewSwitchingContainer').el.dom;
        item.preview(function (result:String, metadata:Object):void {
          loadMask.setVisible(false);
          if (result) {
            metadataValueExpression.setValue(metadata);
            getDisplayField().setValue(result);
            refreshLayout();
          }
          else {
            getActivePreviewExpression().setValue(ItemPreviewPanel.EMPTY_PREVIEW);
          }
        });
      }
      else {
        getActivePreviewExpression().setValue(ItemPreviewPanel.EMPTY_PREVIEW);
      }
    }
  }

  private function refreshLayout():void {
    if (getDisplayField().rendered) {
      var img:* = getDisplayField().el.dom['querySelector']('img');
      if (img) {
        img.addEventListener('load', refreshPreviewLayout);
        return;
      }

      var video:* = getDisplayField().el.dom['querySelector']('video');
      if (video) {
        waitForMedia(video);
        return;
      }

      var embed:* = getDisplayField().el.dom['querySelector']('embed');
      if (embed) {
        embed.addEventListener('load', refreshPreviewLayout);
        return;
      }
    }
  }

  protected function getMetaDataChangedExpression():ValueExpression {
    if (!metadataValueExpression) {
      metadataValueExpression = ValueExpressionFactory.createFromValue(null);
    }
    return metadataValueExpression;
  }

  private function waitForMedia(video:*, timeoutMillis:Number = 0):void {
    if (video.readyState === 4 || timeoutMillis > videoPreviewTimeout) {
      refreshPreviewLayout();
    }
    else {
      window.setTimeout(function ():void {
        waitForMedia(video, (timeoutMillis + videoDelayMillis));
      }, videoDelayMillis);
    }
  }

  private function refreshPreviewLayout():void {
    queryById('connectorPreviewSwitchingContainer').updateLayout();
    queryById('connectorMetaDataPanel').updateLayout();
  }

  /**
   * Returns the itemId of the panel to use for the preview.
   * @param selection the current selection
   */
  private static function resolvePreviewPanel(selection:ConnectorObject):String {
    if (!selection || !(selection is ConnectorItem)) {
      return ItemPreviewPanel.EMPTY_PREVIEW;
    }

    return ItemPreviewPanel.PREVIEW;
  }

  protected function getActivePreviewExpression():ValueExpression {
    if (!activePreviewExpression) {
      activePreviewExpression = ValueExpressionFactory.createFromValue(ItemPreviewPanel.EMPTY_PREVIEW);
    }
    return activePreviewExpression;
  }

  protected function getDisplayField():DisplayField {
    return DisplayField(queryById('connectorPreviewHtmlField'));
  }


  override protected function onDestroy():void {
    selectedItemsValueExpression.removeChangeListener(selectionChanged);
    super.onDestroy();
  }
}
}