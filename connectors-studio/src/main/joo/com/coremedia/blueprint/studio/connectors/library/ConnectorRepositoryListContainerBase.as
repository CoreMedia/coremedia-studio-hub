package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.ui.context.ComponentContextManager;
import com.coremedia.ui.components.SwitchingContainer;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class ConnectorRepositoryListContainerBase extends SwitchingContainer {
  private var selectedItemsValueExpression:ValueExpression;

  public function ConnectorRepositoryListContainerBase(config:ConnectorRepositoryListConainer = null) {
    super(config);
  }

  internal function getActiveViewExpression():ValueExpression {
    var collectionViewModelExpression:ValueExpression = ComponentContextManager.getInstance().getContextExpression(this, CollectionView.MODEL_VARIABLE_NAME);
    return ValueExpressionFactory.createFromFunction(function ():String {
      var model:CollectionViewModel = collectionViewModelExpression.getValue();
      return model ? model.getMainStateBean().get(CollectionViewModel.VIEW_PROPERTY) : undefined;
    })
  }

  protected function getSelectedItemsValueExpression():ValueExpression {
    if(!selectedItemsValueExpression) {
      selectedItemsValueExpression = ComponentContextManager.getInstance().getContextExpression(this, CollectionView.SELECTED_REPOSITORY_ITEMS_VARIABLE_NAME);
    }
    return selectedItemsValueExpression;
  }
}
}
