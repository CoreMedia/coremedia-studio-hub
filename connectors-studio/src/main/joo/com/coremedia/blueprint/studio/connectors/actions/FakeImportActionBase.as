package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.cap.common.Job;
import com.coremedia.cap.common.jobService;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Ext;

public class FakeImportActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  /**
   * @param config
   */
  public function FakeImportActionBase(config:OpenItemtInBrowserTabAction = null) {
    super(Action(Ext.apply({handler: doOpen}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }

  private static function doOpen():void {
    var job:Job = new FakeJob();
    jobService.executeJob(job,
            function ():void {
            },
            function ():void {
            });
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selections:Array = ve.getValue();
    if (!selections) {
      setDisabled(true);
      return;
    }

    setDisabled(true);
    if (selections.length > 1) {
      return;
    }

    setDisabled(false);
  }
}
}
