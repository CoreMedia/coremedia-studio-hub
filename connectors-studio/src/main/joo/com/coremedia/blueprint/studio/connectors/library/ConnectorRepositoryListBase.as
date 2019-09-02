package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorId;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
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

    getStore().on('load', storeDataChanged);
  }

  private function storeDataChanged():void {
    var connectorObject:ConnectorObject = getSelectedNodeExpression().getValue() as ConnectorObject;
    if (!connectorObject) {
      return;
    }

    ConnectorColumnsHelper.refreshColumns(this, connectorObject, function (markAsRead:Boolean):void {
      markAsReadEnabled = markAsRead;

      getView()['emptyText'] = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'empty_selection');
      getView().refresh();

      getHeaderContainer().itemCollection.each(function (col:Column):void {
        if (col['sortState']) {
          var direction:String = col['sortState'];
          var dataIndex = col.dataIndex;
          getStore().sort(dataIndex, direction);
        }
      });
    });
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

  protected function formatDate(date:Object):String {
    return ConnectorHelper.formatDate(getSelectedNodeExpression().getValue(), date);

  }

  private function getCollectionView():CollectionView {
    return Ext.getCmp(CollectionView.COLLECTION_VIEW_ID) as CollectionView;
  }

  internal function getSelectedNodeExpression():ValueExpression {
    if (!selectedNodeExpression) {
      selectedNodeExpression = getCollectionView().getSelectedFolderValueExpression();
    }
    return selectedNodeExpression;
  }

  internal function getSelectedItemsValueExpression():ValueExpression {
    if (!selectedItemsExpression) {
      selectedItemsExpression = getCollectionView().getSelectedRepositoryItemsValueExpression();
      selectedItemsExpression.addChangeListener(markAsRead);
    }
    return selectedItemsExpression;
  }

  internal function markAsRead():void {
    if (!markAsReadEnabled) {
      return;
    }

    var selection:Array = getSelectionModel().getSelection();
    if (selection.length > 0) {
      for each(var item:Model in selection) {
        ConnectorHelper.READ_MARKER.push(item.data.id);
        item.commit(false, ['name']);
      }
    }
  }

  internal function getConnectorItemsValueExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var category:ConnectorCategory = getSelectedNodeExpression().getValue() as ConnectorCategory;
      if (category && !category.getConnector().isLoaded()) {
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
}
}
