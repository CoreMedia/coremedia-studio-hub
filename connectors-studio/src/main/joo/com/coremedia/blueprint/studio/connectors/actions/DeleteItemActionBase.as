package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.studio.connectors.library.ConnectorRepositoryList;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.context.ComponentContextManager;
import com.coremedia.ui.data.ValueExpression;

import ext.Action;
import ext.Component;
import ext.Ext;
import ext.StringUtil;

import mx.resources.ResourceManager;

/**
 * Opens the selection in a new tab
 */
public class DeleteItemActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  private var owner:Component;

  /**
   * @param config
   */
  public function DeleteItemActionBase(config:DeleteItemAction = null) {
    super(Action(Ext.apply({handler: doDelete}, config)));

    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
    setDisabled(true);
  }


  override public function addComponent(comp:Component):void {
    super.addComponent(comp);
    this.owner = comp;
  }

  private function doDelete():void {
    var selection:Array = initialConfig.selectedItemsValueExpression.getValue();
    if (selection && selection.length > 0) {
      clearSelection();

      var title:String = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'delete_item_title');
      var message:String = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'delete_item_message');

      if (selection.length > 1) {
        title = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'delete_items_title');
        message = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'delete_items_message');
      }
      else {
        message = StringUtil.format(message, selection[0].getDisplayName());
      }

      MessageBoxUtil.showConfirmation(title, message, ResourceManager.getInstance().getString('com.coremedia.cms.editor.sdk.actions.Actions', 'Action_delete_buttonText'),
              function (btn:*):void {
                if (btn === 'ok') {
                  deleteSelection(selection);
                }
              });
    }
  }

  private function deleteSelection(selection:Array):void {
    Ext.getCmp(ConnectorRepositoryList.ID).setDisabled(true);
    owner.setDisabled(true);

    var count:Number = selection.length;
    for each(var entity:ConnectorEntity in selection) {
      entity.deleteEntity(function ():void {
        count--;
        if(count === 0) {
          Ext.getCmp(ConnectorRepositoryList.ID).setDisabled(false);
          owner.setDisabled(false);
        }
      }, function ():void {
        Ext.getCmp(ConnectorRepositoryList.ID).setDisabled(false);
        owner.setDisabled(false);

        var errorTitle:String = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'delete_error_title');
        var errorMessage:String = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'delete_error_message');
        errorMessage = StringUtil.format(errorMessage, entity.getDisplayName());
        MessageBoxUtil.showError(errorTitle, errorMessage);
      });
    }
  }

  private static function clearSelection():void {
    var repoList:ConnectorRepositoryList = Ext.getCmp('connectorRepositoryList') as ConnectorRepositoryList;
    ComponentContextManager.getInstance().getContextExpression(repoList, CollectionView.SELECTED_REPOSITORY_ITEMS_VARIABLE_NAME).setValue(null);
  }

  private function selectionChanged(ve:ValueExpression):void {
    setDisabled(true);
    var selections:Array = ve.getValue();
    if(selections == null) {
      return;
    }

    var deleteable:Boolean = selections.length !== 0;
    for each(var selected:ConnectorObject in selections) {
      if (selected is ConnectorEntity) {
        var entity:ConnectorEntity = selected as ConnectorEntity;
        if (!entity.isDeleteable()) {
          deleteable = false;
          break;
        }
      }
      else {
        deleteable = false;
        break;
      }
    }
    setDisabled(!deleteable);
  }
}
}
