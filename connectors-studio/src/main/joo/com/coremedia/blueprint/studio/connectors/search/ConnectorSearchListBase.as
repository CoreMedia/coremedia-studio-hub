package com.coremedia.blueprint.studio.connectors.search {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.library.*;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.ui.data.ValueExpression;

import ext.Ext;

public class ConnectorSearchListBase extends AbstractConnectorList {

  private var selectedNodeExpression:ValueExpression;

  public function ConnectorSearchListBase(config:ConnectorSearchList = null) {
    super(config);
    getSelectedNodeExpression();
  }

  internal function getSelectedNodeExpression():ValueExpression {
    if (!selectedNodeExpression) {
      selectedNodeExpression = getCollectionView().getSelectedFolderValueExpression();
      selectedNodeExpression.addChangeListener(selectedCategoryChanged);
    }

    return selectedNodeExpression;
  }

  private function getCollectionView():CollectionView {
    return Ext.getCmp(CollectionView.COLLECTION_VIEW_ID) as CollectionView;
  }

  protected function formatDate(date:Object):String {
    return ConnectorHelper.formatDate(getSelectedNodeExpression().getValue(), date);
  }

  private function selectedCategoryChanged():void {
    var connectorObject:ConnectorObject = getSelectedNodeExpression().getValue() as ConnectorObject;
    if (!connectorObject) {
      return;
    }

    ConnectorColumnsHelper.refreshColumns(this, connectorObject, Ext.emptyFn);
  }
}
}
