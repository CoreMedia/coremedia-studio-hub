package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.ui.util.DraggableItemsUtils;

import ext.StringUtil;
import ext.Template;
import ext.XTemplate;

import mx.resources.ResourceManager;

public class ConnectorDragDropVisualFeedback {
  private static var simpleDragDropTemplate:Template = new XTemplate(
          '<span>{text:htmlEncode}</span>').compile();

  public static function getHtmlFeedback(items:Array):String {
    if (!items || items.length === 0) {
      return null;
    }

    if (items.length === 1) {
      //the item can be a CatalogObject or a BeanRecord
      var catalogObject:ConnectorObject = (items[0] is ConnectorObject) ? items[0] : items[0].getBean();
      return DraggableItemsUtils.DRAG_GHOST_TEMPLATE.apply({
        title: catalogObject.getName(),
        icon: ConnectorHelper.getTypeCls(catalogObject)
      });
    } else {
      return simpleDragDropTemplate.apply({
        text: StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'DragDrop_multiSelect_text'), items.length)
      });
    }
  }
}
}
