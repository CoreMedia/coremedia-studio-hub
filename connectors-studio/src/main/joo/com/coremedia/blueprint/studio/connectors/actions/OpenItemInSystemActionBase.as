package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Ext;

/**
 * Opens the items URL in the corresponding system.
 */
public class OpenItemInSystemActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  /**
   * @param config
   */
  public function OpenItemInSystemActionBase(config:OpenItemInSystemAction = null) {
    super(Action(Ext.apply({handler: doOpen}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }

  private function doOpen():void {
    var selection:ConnectorItem = initialConfig.selectedItemsValueExpression.getValue()[0];
    var url:String = selection.getManagementUrl();
    if(url) {
      if(url.indexOf("javascript:") !== -1) {
        var start:int = "javascript:".length;
        var script:String = url.substr(start, url.length);
        window['eval'](script);
      }
      else {
        window.open(url, "_blank");
      }
    }
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selections:Array = ve.getValue();
    if (!selections) {
      setDisabled(true);
      return;
    }

    var selection:ConnectorEntity = selections[0] as ConnectorEntity;
    setDisabled(true);
    if (selection) {
      setDisabled(!selection.getManagementUrl());
    }
  }
}
}
