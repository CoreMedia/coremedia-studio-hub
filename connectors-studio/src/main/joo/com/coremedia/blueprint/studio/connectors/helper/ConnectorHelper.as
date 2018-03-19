package com.coremedia.blueprint.studio.connectors.helper {
import com.coremedia.blueprint.studio.connectors.model.Connector;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.sites.SitesService;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

import ext.DateUtil;
import ext.data.Model;

import mx.resources.ResourceManager;

public class ConnectorHelper {
  private static var connectorExpressions:Bean = beanFactory.createLocalBean();

  public static function getChildren(item:ConnectorObject):Array {
    if (item is ConnectorCategory) {
      return ConnectorCategory(item).getChildren();
    }
    if (item is Connector) {
      return (item as Connector).getRootCategories();
    }
    return [];
  }

  public static function getConnectorExpression(connectorType:String):ValueExpression {
    if (!connectorExpressions.get(connectorType)) {
      var ve:ValueExpression = ValueExpressionFactory.createFromFunction(function ():Connector {
        var siteService:SitesService = editorContext.getSitesService();
        var preferredSite:Site = siteService.getPreferredSite();
        if (preferredSite) {
          return beanFactory.getRemoteBean("connector/connector/" + connectorType + "/" + preferredSite.getId()) as Connector;
        }

        return beanFactory.getRemoteBean("connector/connector/" + connectorType + "/all") as Connector;
      });
      connectorExpressions.set(connectorType, ve);
    }

    return connectorExpressions.get(connectorType);
  }

  public static function getTypeLabel(connectorObject:ConnectorObject):String {
    if (connectorObject is Connector) {
      var connector:Connector = connectorObject as Connector;
      var connectorLabel:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'connector_type_' + connector.getConnectorType() + "_name");
      if(!connectorLabel) {
        connectorLabel = connector.getConnectorType();
      }
      return connectorLabel;
    }

    if (connectorObject is ConnectorCategory) {
      var category:ConnectorCategory = connectorObject as ConnectorCategory;
      var label:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'category_type_' + category.getType() + "_name");
      if (label) {
        return label;
      }

      return ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'category_type_folder_name');
    }

    if (connectorObject is ConnectorItem) {
      var item:ConnectorItem = connectorObject as ConnectorItem;
      if (item.getItemType()) {
        var itemKey:String = item.getItemType();
        if(itemKey.indexOf('/') !== -1) {
          itemKey = itemKey.substr(itemKey.indexOf('/')+1, itemKey.length);
        }
        var itemLabel:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'item_type_' + itemKey + '_name');
        if (itemLabel) {
          return itemLabel;
        }

        return camelizeWithWhitespace(item.getItemType());
      }
    }

    return ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Item_label');
  }

  public static function getTypeCls(object:Object):String {
    var icon:String = null;
    if (object is ConnectorObject) {
      var connectorObject:ConnectorObject = object as ConnectorObject;
      if (!connectorObject.isLoaded()) {
        return undefined;
      }

      if(connectorObject is Connector) {
        var connector:Connector = connectorObject as Connector;
        var connectorType:String = connector.getConnectorType();
        icon = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "connector_type_" + connectorType + "_icon");
        if(!icon) {
          icon = ResourceManager.getInstance().getString('com.coremedia.icons.CoreIcons', 'folder_open');
        }

        return icon;
      }

      if (connectorObject is ConnectorItem) {
        var itemKey:String = (connectorObject as ConnectorItem).getItemType();
        if(itemKey.indexOf('/') !== -1) {
          itemKey = itemKey.substr(itemKey.indexOf('/')+1, itemKey.length);
        }

        icon = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "item_type_" + itemKey + "_icon");
        if (icon) {
          return icon;
        }

        var name:String = (connectorObject as ConnectorItem).getName();
        var suffix:String = null;
        if (name.indexOf(".") !== -1) {
          suffix = name.substr(name.lastIndexOf(".") + 1, name.length);
          if (suffix.length === 4) {
            suffix = suffix.substr(0, 3);
          }

          icon = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "item_type_" + suffix + "_icon");
          if (icon) {
            return icon;
          }
        }
        return ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Item_icon');
      }

      if(connectorObject is ConnectorCategory) {
        var category:ConnectorCategory = connectorObject as ConnectorCategory;
        var categoryType:String =  category.getType();
        return ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'category_type_' + categoryType  + '_icon');
      }
    }
  }

  public static function camelizeWithWhitespace(str:String):String {
    return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function (letter, index) {
      return letter.toUpperCase();
    }).replace(/\s+/g, ' ');
  }

  public static function formatDate(date:*):String {
    if (date) {
      return DateUtil.format(date, ResourceManager.getInstance().getString('com.coremedia.cms.editor.Editor', 'dateFormat'));
    } else {
      return "";
    }
  }

  public static function formatFileSize(size:Number):String {
    if (size) {
      var i:int = Math.floor(Math.log(size) / Math.log(1024));
      return Number(( size / Math.pow(1024, i) ).toFixed(2)) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
    }
    return "";
  }

  public static function fileSizeRenderer(value:*, _:*, record:Model):String {
    if(value) {
      return formatFileSize(value);
    }
    return "";
  }
}
}
