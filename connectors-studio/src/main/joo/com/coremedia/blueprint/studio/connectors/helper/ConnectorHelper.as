package com.coremedia.blueprint.studio.connectors.helper {
import com.coremedia.blueprint.studio.connectors.model.Connection;
import com.coremedia.blueprint.studio.connectors.model.Connector;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategoryImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorContext;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntityImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObjectImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorPropertyNames;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.sdk.columns.grid.TypeIconColumn;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ui.bem.IconWithTextBEMEntities;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EncodingUtil;
import com.coremedia.ui.util.EventUtil;

import ext.DateUtil;
import ext.Ext;
import ext.data.Model;
import ext.grid.GridPanel;
import ext.grid.column.Column;

import mx.resources.ResourceManager;

public class ConnectorHelper {
  public static const TYPE_COREMEDIA_CONNECTOR:String = "coremedia";
  public static const READ_MARKER:Array = [];
  private static var connectorExpressions:Bean = beanFactory.createLocalBean();
  private static var topLevelCategoriesExpressions:ValueExpression;

  public static function getTopLevelCategoriesExpression():ValueExpression {
    if(!topLevelCategoriesExpressions) {
      topLevelCategoriesExpressions = ValueExpressionFactory.createFromFunction(function ():Array {
        var connectorTypes:Array = ConnectorHelper.getConnectorTypesExpression().getValue();
        if (connectorTypes === undefined) {
          return undefined;
        }

        var connectors:Array = [];
        for each(var cType:Object in connectorTypes) {
          var connectorType:String = cType.name;
          var connectorExpression:ValueExpression = ConnectorHelper.getConnectorExpression(connectorType);
          var connector:Connector = connectorExpression.getValue();
          if (connector === undefined) {
            return undefined;
          }

          if (!connector.isLoaded()) {
            connector.load();
            return undefined;
          }

          connectors.push(connector);
        }

        var result:Array = [];
        for each(var c:Connector in connectors) {
          var rootCategories:Array = c.getRootCategories();
          //pre-load root nodes since the intermediate notes are hidden, see README.md
          for each(var root:ConnectorCategoryImpl in rootCategories) {
            if (!root.isLoaded()) {
              root.load();
              return undefined;
            }

            if (root.getConnector() != null) {
              if(!root.getConnector().isLoaded()) {
                root.getConnector().load();
                return undefined;
              }

              result.push(root);
            }
          }
        }

        return result;
      });
    }

    return topLevelCategoriesExpressions;
  }

  public static function getConnectorObject(connectorId:String):ConnectorObject {
    //double encoding!
    var formattedId:String = encodeURIComponent(connectorId);
    formattedId = encodeURIComponent(formattedId);

    var uriPath:String = 'connector/item/' + formattedId;
    if (connectorId.indexOf('/category/') !== -1) {
      uriPath = 'connector/category/' + formattedId;
    }

    var bean:ConnectorObjectImpl = beanFactory.getRemoteBean(uriPath) as ConnectorObjectImpl;
    bean.load();
    return bean;
  }

  public static function getType(entity:ConnectorObject):String {
    if (entity is Connector) {
      return ConnectorPropertyNames.TYPE_CONNECTOR;
    }

    if (entity is ConnectorCategory) {
      return ConnectorPropertyNames.TYPE_CONNECTOR_CATEGORY;
    }

    if (entity is ConnectorItem) {
      return ConnectorPropertyNames.TYPE_CONNECTOR_ITEM;
    }
    return null;
  }

  public static function getChildren(item:ConnectorObject):Array {
    if (item is ConnectorCategory) {
      return ConnectorCategory(item).getChildren();
    }
    if (item is Connector) {
      return (item as Connector).getRootCategories();
    }
    return [];
  }

  public static function onSiteSelectionChange():void {
    var beans:Object = connectorExpressions.toObject();
    for each(var item:Object in beans) {
      var fve:ValueExpression = item as ValueExpression;
      var connector:Connector = fve.getValue();
      connector.invalidate(function (loadedConnector:Connector):void {
        var connectorType:String = loadedConnector.getConnectorType();
        if (connectorType === "navigation") {
          var roots:Array = loadedConnector.getRootCategories();
          for each(var root:ConnectorCategory in roots) {
            root.invalidate();
          }
        }
      });
    }
  }

  public static function getConnectorExpression(connectorType:String):ValueExpression {
    if (!connectorExpressions.get(connectorType)) {
      var ve:ValueExpression = ValueExpressionFactory.createFromFunction(function ():Connector {
        var siteService:SitesService = editorContext.getSitesService();
        var preferredSite:Site = siteService.getPreferredSite();
        if (preferredSite) {
          var connector:Connector = beanFactory.getRemoteBean("connector/connector/" + connectorType + "/" + preferredSite.getId()) as Connector;
          return connector;
        }

        return beanFactory.getRemoteBean("connector/connector/" + connectorType + "/all") as Connector;
      });
      connectorExpressions.set(connectorType, ve);
    }

    return connectorExpressions.get(connectorType);
  }

  public static function getConnectorTypesExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var typesBean:RemoteBean = beanFactory.getRemoteBean("connector/connectors/types");
      if (!typesBean.isLoaded()) {
        typesBean.load();
        return undefined;
      }

      return typesBean.get("items");
    });
  }

  public static function getTypeLabel(connectorObject:ConnectorObject):String {
    if (!(connectorObject is ConnectorObject) || !connectorObject.isLoaded()) {
      return undefined;
    }

    var label:String = connectorObject.getTypeLabel();
    if (!label) {
      label = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Item_label');
    }

    return EncodingUtil.encodeForHTML(label);
  }

  public static function getTypeCls(connectorObject:ConnectorObject):String {
    if (!(connectorObject is ConnectorObject) || !connectorObject.isLoaded()) {
      return undefined;
    }

    return connectorObject.getTypeCls();
  }

  public static function camelizeWithWhitespace(str:String):String {
    return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function (letter:*, index:*):String {
      return letter.toUpperCase();
    }).replace(/\s+/g, ' ');
  }

  public static function formatDate(entity:*, date:*):String {
    var context:ConnectorContext = null;
    if (entity is ConnectorEntity) {
      context = entity.getContext();
    } else if (entity is Connector) {
      var connectionData:Object = (entity as ConnectorImpl).get(ConnectorPropertyNames.CONNECTIONS)[0];
      var connection:Connection = new Connection(connectionData);
      context = connection.getContext();
    }

    if (date && context) {
      var format:String = context.getDateFormat();
      if (format === "short") {
        return DateUtil.format(date, ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'shortDateFormat'));
      }

      return DateUtil.format(date, ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'dateFormat'));
    } else {
      return "";
    }
  }

  public static function formatFileSize(size:Number):String {
    if (size) {
      var i:int = Math.floor(Math.log(size) / Math.log(1024));
      return Number((size / Math.pow(1024, i)).toFixed(2)) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
    }
    return "";
  }

  public static function nameRenderer(value:*, _:*, record:BeanRecord):String {
    var connectorObject:ConnectorObject = record.getBean() as ConnectorObject;
    if (!(connectorObject is ConnectorObject) || !connectorObject.isLoaded()) {
      return undefined;
    }

    var textCls:String = connectorObject.getTextCls();
    if (textCls) {
      return '<span class="' + textCls + '">' + value + '</span>';
    }

    return value;
  }

  public static function fileSizeRenderer(value:*, _:*, record:Model):String {
    if (value) {
      if (value && value is String) {
        return value;
      }
      return formatFileSize(value);
    }
    return "";
  }

  //--------------------- Column stuff ---------------------------------------------------------------------------------
  public static function createColumns(connectorObject:ConnectorObject, markAsRead:Boolean = false):Array {
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
        renderer: renderTitle,
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
        renderer: nameRenderer,
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
          renderer: ConnectorHelper.customColumnRenderer
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


  /**
   * Rendered for the title column, remove the bold format of
   * an entry is this was already selected.
   * @param value the name of the item
   * @param metaData the metadata that contains the state of the record
   * @param record the actual bean record
   * @return the string to be rendered
   */
  public static function renderTitle(value:*, metaData:*, record:BeanRecord):String {
    if (value === undefined) {
      return "...";
    }

    value = EncodingUtil.encodeForHTML(value);
    var item:ConnectorEntity = record.getBean() as ConnectorEntity;
    if (item is ConnectorItem) {
      if (isUnread(record.data.id)) {
        return '<b>' + value + '<b/>';
      }
    }
    return value;
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
        grid.reconfigure(grid.getStore(), ConnectorHelper.createColumns(connectorObject, markAsRead));
        callback(markAsRead);
      });
    });
  }

  public static function isIgnoredForSearch(ct:ContentType):Boolean {
    var excludedTypes:Array = editorContext.getContentTypesExcludedFromSearch();
    for each(var cTypeName:ContentType in excludedTypes) {
      if (cTypeName === ct.getName()) {
        return true;
      }
    }
    return false;
  }
}
}
