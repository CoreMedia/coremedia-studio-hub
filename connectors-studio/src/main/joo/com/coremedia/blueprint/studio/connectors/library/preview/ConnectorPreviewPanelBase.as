package com.coremedia.blueprint.studio.connectors.library.preview {
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.ICollectionView;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.skins.LoadMaskSkin;

import ext.Ext;

import ext.LoadMask;
import ext.form.field.DisplayField;
import ext.panel.Panel;

public class ConnectorPreviewPanelBase extends Panel {

  private var activePreviewExpression:ValueExpression;
  private var loadMask:LoadMask;
  private const videoDelayMillis:Number = 500;
  private const videoPreviewTimeout:Number = 10000;


  //additional ignored doctypes for previewing that are not part of the editorContext
  private const IGNORED_DOCTYPES:Array = ["CMExternalLink"];

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  public var selectedItemValueExpression:ValueExpression;
  public var selectedNodeExpression:ValueExpression;
  public var metadataValueExpression:ValueExpression;


  public function ConnectorPreviewPanelBase(config:ConnectorPreviewPanel = null) {
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
    getSelectedNodeExpression().addChangeListener(selectionChanged);
  }

  internal function getSelectedNodeExpression():ValueExpression {
    if (!selectedNodeExpression) {
      selectedNodeExpression = getCollectionView().getSelectedFolderValueExpression();
    }
    return selectedNodeExpression;
  }

  private function getCollectionView():CollectionView {
    return Ext.getCmp(CollectionView.COLLECTION_VIEW_ID) as CollectionView;
  }

  protected function getSelectedItemExpression():ValueExpression {
    if (!selectedItemValueExpression) {
      selectedItemValueExpression = ValueExpressionFactory.createFromValue(null);
    }
    return selectedItemValueExpression;
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selection:Object = ve.getValue();
    if ((!selection || selection.length === 0) && getSelectedNodeExpression().getValue()) {
      selection = getSelectedNodeExpression().getValue();
    }

    if (selection is Array) {
      selection = selection[0];
    }

    getSelectedItemExpression().setValue(selection);
    getDisplayField().setValue('<i>' + resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'preview_loading') + '...</i>');

    if (selection is ConnectorEntity) {
      var entity:ConnectorEntity = selection as ConnectorEntity;
      if (!isPreviewable(entity)) {
        getDisplayField().setValue(resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'empty_preview'));
        return;
      }

      getActivePreviewExpression().setValue(ConnectorPreviewPanel.PREVIEW);
      loadMask.setVisible(true);

      var dom:* = queryById('connectorPreviewSwitchingContainer').el.dom;
      entity.preview(function (result:String, metadata:Object):void {
        loadMask.setVisible(false);
        if (result || metadata) {
          if (metadata) {
            metadataValueExpression.setValue(metadata);
          } else {
            metadataValueExpression.setValue({});
          }

          if (result) {
            getDisplayField().setValue(result);
          } else {
            getDisplayField().setValue(resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'empty_preview'));
          }

          refreshLayout();
        } else {
          getActivePreviewExpression().setValue(ConnectorPreviewPanel.EMPTY_PREVIEW);
        }
      });
    } else {
      getActivePreviewExpression().setValue(ConnectorPreviewPanel.EMPTY_PREVIEW);
    }
  }

  /**
   * Special check for connector items that are based on content.
   * We check if the content type should be ignored for previewing
   * since this is already configured in the editorContext.
   */
  private function isPreviewable(entity:ConnectorEntity):Boolean {
    var columnValues:Array = entity.getColumnValues();

    //still loading?
    if (!columnValues) {
      return false;
    }

    for each(var cValue:Object in columnValues) {
      if (cValue.dataIndex === "docType") {
        var docType:String = cValue.value;
        if (editorContext.getDocumentTypesWithoutPreview().indexOf(docType) !== -1) {
          return false;
        }
        if (IGNORED_DOCTYPES.indexOf(docType) !== -1) {
          return false;
        }
      }
    }
    return true;
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
    } else {
      window.setTimeout(function ():void {
        waitForMedia(video, (timeoutMillis + videoDelayMillis));
      }, videoDelayMillis);
    }
  }

  private function refreshPreviewLayout():void {
    queryById('connectorPreviewSwitchingContainer').updateLayout();
    queryById('connectorMetaDataPanel').updateLayout();
  }

  protected function getActivePreviewExpression():ValueExpression {
    if (!activePreviewExpression) {
      activePreviewExpression = ValueExpressionFactory.createFromValue(ConnectorPreviewPanel.EMPTY_PREVIEW);
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
