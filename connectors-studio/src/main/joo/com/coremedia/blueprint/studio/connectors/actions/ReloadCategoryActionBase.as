package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.studio.connectors.library.ConnectorRepositoryList;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Component;
import ext.Ext;

/**
 * Invalidate a category
 */
public class ReloadCategoryActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  private var owner:Component;

  /**
   * @param config
   */
  public function ReloadCategoryActionBase(config:ReloadCategoryAction = null) {
    super(Action(Ext.apply({handler: reload}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }


  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    this.owner = comp;
  }

  private function reload():void {
    var selection:ConnectorObject = initialConfig.selectedItemsValueExpression.getValue()[0];
    if(selection is ConnectorCategory) {
      Ext.getCmp(ConnectorRepositoryList.ID).setDisabled(true);
      owner.setDisabled(true);
      (selection as ConnectorCategory).refresh(function():void {
        Ext.getCmp(ConnectorRepositoryList.ID).setDisabled(false);
        owner.setDisabled(false);
      });
    }
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selections:Array = ve.getValue();
    if (!selections) {
      setDisabled(true);
      return;
    }

    var selection:ConnectorObject = selections[0];
    setDisabled(!(selection && selection is ConnectorCategory));
  }
}
}
