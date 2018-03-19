package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Ext;

/**
 * Download blob out of the selection
 */
public class DownloadItemActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  /**
   * @param config
   */
  public function DownloadItemActionBase(config:DownloadItemAction = null) {
    super(Action(Ext.apply({handler: download}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }

  private function download():void {
    var selection:Array = initialConfig.selectedItemsValueExpression.getValue();
    for each(var item:ConnectorItem in selection) {
      item.download();
    }
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selection:Array = ve.getValue();
    if (!selection) {
      setDisabled(true);
      return;
    }

    var downloadable:Boolean = selection.length !== 0;
    for each(var selected:ConnectorObject in selection) {
      if (selected is ConnectorItem) {
        var entity:ConnectorItem = selected as ConnectorItem;
        if (!entity.isDownloadable()) {
          downloadable = false;
          break;
        }
      }
      else {
        downloadable = false;
        break;
      }
    }
    setDisabled(!downloadable);
  }
}
}
