package com.coremedia.blueprint.studio.connectors.library {

import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.ICollectionView;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.skins.LoadMaskSkin;

import ext.Ext;
import ext.LoadMask;

import ext.container.Container;

import mx.resources.ResourceManager;

public class ConnectorRepositoryThumbnailsBase extends Container {

  private var selectedNodeExpression:ValueExpression;
  private var selectedItemsValueExpression:ValueExpression;
  private var loadMask:LoadMask;

  public function ConnectorRepositoryThumbnailsBase(config:ConnectorRepositoryThumbnails = null) {
    super(config);
  }


  override protected function afterRender():void {
    super.afterRender();

    var loadMaskCfg:LoadMask = LoadMask({
      target: this,
      ui: LoadMaskSkin.LIGHT.getSkin()
    });
    loadMaskCfg.msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'refreshing_message');
    loadMask = new LoadMask(loadMaskCfg);
    loadMask.disable();
  }

  override public function setDisabled(disabled:Boolean):void {
    super.setDisabled(disabled);
    if(loadMask) {
      loadMask.setVisible(disabled);
    }
  }

  internal function getConnectorItemsValueExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      return ConnectorHelper.getChildren(getSelectedNodeExpression().getValue());
    });
  }

  protected function getSelectedItemsValueExpression():ValueExpression {
    if (!selectedItemsValueExpression) {
      selectedItemsValueExpression = getCollectionView().getSelectedRepositoryItemsValueExpression();
    }
    return selectedItemsValueExpression;
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

}
}
