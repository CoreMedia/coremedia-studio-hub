package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.Connector;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorId;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.context.ComponentContextManager;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.skins.LoadMaskSkin;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EventUtil;

import ext.Ext;
import ext.LoadMask;
import ext.data.Model;
import ext.data.Store;
import ext.grid.column.Column;

import mx.resources.ResourceManager;

public class ConnectorRepositoryListBase extends AbstractConnectorList {
  private static const READ_MARKER:Array = [];
  private var selectedNodeExpression:ValueExpression;
  private var selectedItemsExpression:ValueExpression;

  private var loadMask:LoadMask;
  private var markAsReadEnabled:Boolean = false;

  public function ConnectorRepositoryListBase(config:ConnectorRepositoryList = null) {
    super(config);
  }


  override protected function afterRender():void {
    super.afterRender();

    var loadMaskCfg:LoadMask = LoadMask({
      target: this,
      ui: LoadMaskSkin.LIGHT.getSkin()
    });
    loadMaskCfg.msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'refreshing_message');
    loadMask = new LoadMask(loadMaskCfg);
    loadMask.disable();
  }

  override public function setDisabled(disabled:Boolean):void {
    super.setDisabled(disabled);
    loadMask.setVisible(disabled);
  }

  /**
   * Implementation of the preview helper 'showItem' method.
   * This allows to select other items of the connector framework.
   * @param id the connectorId of the item to show.
   */
  public function showItem(id:String):void {
    var connectorId:ConnectorId = new ConnectorId(id);
    var item:ConnectorEntity = connectorId.toConnectorEntity();
    item.load(function (loadedItem:*):void {
      // ignoring type of entity (show in repository doesn't really care if it's of type Content)
      editorContext.getCollectionViewManager().showInRepository(loadedItem);
      EventUtil.invokeLater(function ():void {
        var rowNumber:Number = findBeanRecordIndex(getStore(), loadedItem);
        var row:* = getView().getRow(rowNumber);
        if (row) {
          row.scrollIntoView();
          markAsRead();
        }
      });
    });
  }

  internal function getSelectedNodeExpression():ValueExpression {
    if (!selectedNodeExpression) {
      selectedNodeExpression = ComponentContextManager.getInstance().getContextExpression(this, CollectionView.SELECTED_FOLDER_VARIABLE_NAME);
      selectedNodeExpression.addChangeListener(selectedCategoryChanged);
    }

    return selectedNodeExpression;
  }

  internal function getSelectedItemsValueExpression():ValueExpression {
    if (!selectedItemsExpression) {
      selectedItemsExpression = ComponentContextManager.getInstance().getContextExpression(this, CollectionView.SELECTED_REPOSITORY_ITEMS_VARIABLE_NAME);
      selectedItemsExpression.addChangeListener(markAsRead);
    }
    return ComponentContextManager.getInstance().getContextExpression(this, CollectionView.SELECTED_REPOSITORY_ITEMS_VARIABLE_NAME);
  }

  internal function markAsRead():void {
    if(!markAsReadEnabled) {
      return;
    }

    var selection:Array = getSelectionModel().getSelection();
    if(selection.length > 0) {
      for each(var item:Model in selection) {
        READ_MARKER.push(item.data.id);
        item.commit(false);
      }
    }
  }

  private function selectedCategoryChanged():void {
    getView()['emptyText'] = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'empty_selection');
    getView().refresh();

    var columns:Array = getColumns();
    var category:ConnectorCategory = getSelectedNodeExpression().getValue() as ConnectorCategory;
    if (category) {
      category.getConnector().load(function():void {
        markAsReadEnabled = category.getContext().isMarkAsReadEnabled();
      });

      if(category.getItems().length > 0) {
        var item:ConnectorItem = category.getItems()[0];
        item.load(function ():void {
          for each(var c:Column in columns) {
            if(c.dataIndex === "status" || c.dataIndex === "size") {
              c.setHidden(true);
            }

            if(c.dataIndex === "status" && item.getStatus()) {
              c.setHidden(false);
            }
            else if(c.dataIndex === "size" && item.getSize()) {
              c.setHidden(false);
            }
          }
        });
      }
    }
  }


  /**
   * Rendered for the title column, remove the bold format of
   * an entry is this was already selected.
   * @param value the name of the item
   * @param metaData the metadata that contains the state of the record
   * @param record the actual bean record
   * @return the string to be rendered
   */
  protected function renderTitle(value:*, metaData:*, record:BeanRecord):String {
    if(value === undefined) {
      return "...";
    }

    var item:ConnectorEntity = record.getBean() as ConnectorEntity;
    if(item is ConnectorItem) {
      if (markAsReadEnabled && isUnread(record.data.id)) {
        return '<b>' + value + '<b/>';
      }
    }
    return value;
  }

  internal function getConnectorItemsValueExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var category:ConnectorCategory = getSelectedNodeExpression().getValue() as ConnectorCategory;
      if(category && !category.getConnector().isLoaded()) {
        category.getConnector().load();
        return undefined;
      }
      return ConnectorHelper.getChildren(getSelectedNodeExpression().getValue());
    });
  }


  /**
   * ============================= Static Helper Method ========================================
   */

  /**
   * Find index of BeanRecord with given bean in the given store.
   */
  internal static function findBeanRecordIndex(store:Store, bean:Bean):Number {
    if (bean) {
      return store.findBy(function (record:BeanRecord):Boolean {
        return record.getBean() === bean;
      });
    }
    return -1;
  }

  internal static function formatExternalId(externalId:String):String {
    var url:Object = Ext.urlDecode(externalId);
    return externalId;
  }



  /**
   * Returns true if the given id was already selected by the user.
   * @param id
   * @return
   */
  internal static function isUnread(id:*):Boolean {
    for (var i:int = 0; i < READ_MARKER.length; i++) {
      if (READ_MARKER[i] === id) {
        return false;
      }
    }
    return true;
  }
}
}
