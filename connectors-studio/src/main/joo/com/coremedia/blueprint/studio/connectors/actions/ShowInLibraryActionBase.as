package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Ext;

/**
 * Opens the selection in a new tab
 */
public class ShowInLibraryActionBase extends Action {

  [Bindable]
  public var selectedValuesExpression:ValueExpression;

  private var ve:ValueExpression;

  /**
   * @param config
   */
  public function ShowInLibraryActionBase(config:ShowInLibraryAction = null) {
    super(Action(Ext.apply({handler: doShow}, config)));
    this.ve = config.selectedValuesExpression;
    this.ve.addChangeListener(updateDisabled);
    this.updateDisabled(this.ve);
  }

  private function updateDisabled(ve:ValueExpression):void {
    var value:* = ve.getValue();
    this.setDisabled(!value || value.length === 0);
  }

  private function doShow():void {
    var value:* = ve.getValue();
    if(value && value.length > 0) {
      var v:* = value[0];
      editorContext.getCollectionViewManager().showInRepository(v);
    }
  }
}
}
