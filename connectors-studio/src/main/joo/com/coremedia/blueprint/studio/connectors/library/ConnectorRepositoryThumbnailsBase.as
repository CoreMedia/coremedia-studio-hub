package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.context.ComponentContextManager;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

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
      selectedItemsValueExpression = ComponentContextManager.getInstance().getContextExpression(this, CollectionView.SELECTED_REPOSITORY_ITEMS_VARIABLE_NAME);
    }
    return selectedItemsValueExpression;
  }

  internal function getSelectedNodeExpression():ValueExpression {
    if (!selectedNodeExpression) {
      selectedNodeExpression = ComponentContextManager.getInstance().getContextExpression(this, CollectionView.SELECTED_FOLDER_VARIABLE_NAME);
    }

    return selectedNodeExpression;
  }
}
}