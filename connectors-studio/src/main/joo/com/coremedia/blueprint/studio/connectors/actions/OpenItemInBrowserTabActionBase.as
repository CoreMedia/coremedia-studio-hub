package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Ext;

/**
 * Opens the selection in a new tab
 */
public class OpenItemInBrowserTabActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  /**
   * @param config
   */
  public function OpenItemInBrowserTabActionBase(config:OpenItemtInBrowserTabAction = null) {
    super(Action(Ext.apply({handler: doOpen}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }

  private function doOpen():void {
    var selection:ConnectorItem = initialConfig.selectedItemsValueExpression.getValue()[0];
    selection.openInTab();
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selections:Array = ve.getValue();
    if (!selections) {
      setDisabled(true);
      return;
    }

    setDisabled(true);
    if(selections.length > 1) {
      return;
    }

    var selected:ConnectorObject = selections[0];
    if (selected && selected is ConnectorItem) {
      var item:ConnectorItem = selected as ConnectorItem;
      setDisabled(!item.getOpenInTabUrl());
    }
  }
}
}
