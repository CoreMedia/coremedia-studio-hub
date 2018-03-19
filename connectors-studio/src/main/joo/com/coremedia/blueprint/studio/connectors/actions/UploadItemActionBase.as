package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.cms.editor.sdk.upload.UploadSettings;
import com.coremedia.cms.editor.sdk.upload.dialog.UploadDialog;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Ext;

/**
 * Upload for categories
 */
public class UploadItemActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  private var uploadSettings:UploadSettings;

  /**
   * @param config
   */
  public function UploadItemActionBase(config:UploadItemAction = null) {
    super(Action(Ext.apply({handler: upload}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }

  private function upload():void {
    var selection:ConnectorObject = initialConfig.selectedItemsValueExpression.getValue()[0];

    var dialog:UploadDialog = new UploadDialog(UploadDialog({
      settings: getUploadSettings(),
      customFileWrapperFactoryMethod : createFileWrapper,
      callback:function():void {
        if(selection is ConnectorCategory) {
          (selection as ConnectorCategory).refresh();
        }
      }
    }));
    dialog.show();
  }

  private function createFileWrapper(file:*):FileWrapper {
    var selection:ConnectorCategory = initialConfig.selectedItemsValueExpression.getValue()[0];
    var wrapper:FileWrapper = new FileWrapper(file);
    wrapper.setCustomUploadUrl(selection.getUploadUri());
    return wrapper;
  }

  private function getUploadSettings():UploadSettings {
    if (!uploadSettings) {
      uploadSettings = new UploadSettings();
    }
    return uploadSettings;
  }

  private function selectionChanged(ve:ValueExpression):void {
    var selections:Array = ve.getValue();
    if (!selections) {
      setDisabled(true);
      return;
    }

    var selection:ConnectorObject = selections[0];
    setDisabled(true);
    if (selection && selection is ConnectorCategory) {
      var category:ConnectorCategory = selection as ConnectorCategory;
      setDisabled(!category.isWriteable());
    }
  }
}
}
