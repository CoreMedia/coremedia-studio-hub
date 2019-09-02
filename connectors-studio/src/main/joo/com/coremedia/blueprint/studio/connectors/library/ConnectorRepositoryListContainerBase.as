package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.ICollectionView;
import com.coremedia.ui.components.SwitchingContainer;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class ConnectorRepositoryListContainerBase extends SwitchingContainer {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  [Bindable]
  public var collectionView:ICollectionView;

  private var activeViewExpression:ValueExpression;

  public function ConnectorRepositoryListContainerBase(config:ConnectorRepositoryListContainer = null) {
    super(config);
  }

  internal function getActiveViewExpression(config:ConnectorRepositoryListContainer):ValueExpression {
    if (!activeViewExpression) {
      activeViewExpression = ValueExpressionFactory.create(CollectionViewModel.VIEW_PROPERTY, collectionView.getCollectionViewModel().getMainStateBean());
    }
    return activeViewExpression;
  }

}
}
