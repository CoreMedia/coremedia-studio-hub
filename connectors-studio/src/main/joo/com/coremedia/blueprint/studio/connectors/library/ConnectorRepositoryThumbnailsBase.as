package com.coremedia.blueprint.studio.connectors.library {

import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.ICollectionView;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Ext;

import ext.container.Container;

public class ConnectorRepositoryThumbnailsBase extends Container {

  private var selectedNodeExpression:ValueExpression;
  private var selectedItemsValueExpression:ValueExpression;

  public function ConnectorRepositoryThumbnailsBase(config:ConnectorRepositoryThumbnails = null) {
    super(config);
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
