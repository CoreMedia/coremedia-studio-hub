package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.Connection;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorContext;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntityImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.columns.grid.TypeIconColumn;
import com.coremedia.ui.bem.IconWithTextBEMEntities;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EventUtil;

import ext.Ext;
import ext.data.Model;
import ext.grid.GridPanel;
import ext.grid.column.Column;

import mx.resources.ResourceManager;

/**
 * Helper class to update the column depending on the selected connector object.
 */
public class ConnectorColumnsHelper {

  public static function refreshColumns(grid:GridPanel, connectorObject:ConnectorObject, callback:Function):void {
    ValueExpressionFactory.createFromFunction(function ():Boolean {
      if (!connectorObject.isLoaded()) {
        return undefined;
      }
      if (!connectorObject.getConnector().isLoaded()) {
        return undefined;
      }
      return true;
    }).loadValue(function ():void {
      //mmmh, yes, I know, but this ensures that the folder selection is completed before updating the column model
      EventUtil.invokeLater(function ():void {
        grid.getHeaderContainer()['_usedIDs'] = {};//ignore warnings
        var category:ConnectorCategory = connectorObject as ConnectorCategory;
        var markAsRead:Boolean = category && category.getContext().isMarkAsReadEnabled();
        grid.reconfigure(grid.getStore(), createColumns(connectorObject, markAsRead));
        callback(markAsRead);
      });
    });
  }


  private static function createColumns(connectorObject:ConnectorObject, markAsRead:Boolean = false):Array {
    var connector:ConnectorImpl = connectorObject.getConnector() as ConnectorImpl;
    var category:ConnectorCategory = connectorObject as ConnectorCategory;
    var context:ConnectorContext = new ConnectorContext({});
    if (category) {
      var connection:Connection = connector.getConnection(category.getConnectionId());
      context = connection.getContext();
    }

    var cols:Array = [];

    var typeCol:Column = Ext.create(TypeIconColumn, {
      width: 125,
      sortField: 'type',
      showTypeName: true,
      resizable: true
    });
    cols.push(typeCol);

    var title:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'name_header');
    if (markAsRead) {
      var markableColumn:Column = Ext.create(Column, {
        width: 300,
        hidden: context.isColumnHidden('name'),
        dataIndex: 'name',
        header: title,
        resizable: true,
        defaultSortColumn: true,
        renderer: ConnectorHelper.renderTitle,
        flex: 1
      });
      cols.push(markableColumn);
    } else {
      var nameCol:Column = Ext.create(Column, {
        width: 300,
        hidden: context.isColumnHidden('name'),
        dataIndex: 'name',
        header: title,
        resizable: true,
        renderer: ConnectorHelper.nameRenderer,
        defaultSortColumn: true,
        flex: 1
      });
      cols.push(nameCol);
    }


    title = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'size_header');
    var sizeCol:Column = Ext.create(Column, {
      width: 80,
      hidden: context.isColumnHidden('size'),
      stateId: 'size',
      dataIndex: 'size',
      header: title,
      resizable: true,
      sortable: true,
      hideable: true,
      renderer: ConnectorHelper.fileSizeRenderer
    });
    cols.push(sizeCol);

    //******* Custom columns ***************/
    if (category) {
      var columns:Array = category.getColumns();
      for each(var c:Object in columns) {
        var customTitle:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', c.title);
        if (!customTitle) {
          customTitle = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', c.title + "_header");
          if (!customTitle) {
            customTitle = c.title;
          }
        }

        var customColumn:Column = Ext.create(Column, {
          width: c.width, stateId: c.dataIndex, dataIndex: c.dataIndex,
          header: customTitle, resizable: c.resizable, sortable: c.sortable, hideable: c.hideable,
          renderer: customColumnRenderer
        });
        if (c.index >= 0) {
          cols.splice(c.index, 0, customColumn);
        } else {
          cols.push(customColumn);
        }
      }
    }

    title = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'modified_header');
    var modCol:Column = Ext.create(Column, {
      width: 160,
      hidden: context.isColumnHidden('lastModified'),
      stateId: 'lastModified',
      dataIndex: 'lastModified',
      header: title,
      resizable: true,
      sortable: true,
      hideable: true
    });
    cols.push(modCol);

    return cols;
  }


  private static function customColumnRenderer(value:*, metaData:Object, record:Model):String {
    var item:ConnectorEntityImpl = (record as BeanRecord).getBean() as ConnectorEntityImpl;
    if (!item) {
      return "";
    }

    var dataIndex:String = metaData.column.dataIndex;
    var columnValue:Object = item.getColumnValue(dataIndex);
    if (columnValue === undefined) {
      return undefined;
    }

    if (columnValue === null) {
      return "";
    }

    if (columnValue.icon) {
      var iconText:String = columnValue.iconText || "";
      var iconTooltip:String = columnValue.iconTooltip || "";
      var iconCls:String = ResourceManager.getInstance().getString('com.coremedia.icons.CoreIcons', columnValue.value);
      var html:String = '<div aria-label="' + iconText + '" class="' + IconWithTextBEMEntities.BLOCK + '" data-qtip="' + iconTooltip + '">' +
              '<span class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' ' + iconCls + '"></span>' +
              '<span style="width: 0px;position:absolute;overflow:hidden;">' + iconTooltip + '</span>' +
              '<span class="' + IconWithTextBEMEntities.ELEMENT_TEXT + '">' + iconText + '</span>' +
              '</div>';
      return html;
    }

    if (columnValue) {
      return columnValue.value;
    }
    return "";
  }

}
}
