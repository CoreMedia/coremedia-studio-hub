package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.ui.util.DraggableItemsUtils;

import ext.StringUtil;
import ext.Template;
import ext.XTemplate;

import mx.resources.ResourceManager;

/**
 * A helper class to create drag and drop visual feedback HTML
 */
[ResourceBundle('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin')]
public class ConnectorDragDropVisualFeedback {

  private static var simpleDragDropTemplate:Template = new XTemplate(
          '<span>{text:htmlEncode}</span>').compile();

  public static function getHtmlFeedback(items:Array):String {
    if (!items || items.length === 0) {
      return null;
    }

    if (items.length === 1) {
      var entity:ConnectorEntity = (items[0] is ConnectorEntity) ? items[0] : items[0].getBean();
      return DraggableItemsUtils.DRAG_GHOST_TEMPLATE.apply({
        title: entity.getName(),
        icon: null
      });
    } else {
      return simpleDragDropTemplate.apply({
        text: StringUtil.format(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'DragDrop_multiSelect_text'), items.length)
      });
    }
  }
}
}
