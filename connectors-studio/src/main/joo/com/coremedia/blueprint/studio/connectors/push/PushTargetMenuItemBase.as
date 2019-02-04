package com.coremedia.blueprint.studio.connectors.push {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorContext;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.menu.Item;

public class PushTargetMenuItemBase extends Item {

  [Bindable]
  public var category:ConnectorCategory;

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  private var bindDisableExpression:ValueExpression;

  public function PushTargetMenuItemBase(config:PushTargetMenuItem = null) {
    super(config);
  }

  protected function openPushDialog():void {
    var dialog:PushDialog = new PushDialog(PushDialog({
      'rootCategory': category,
      'selectedItemsValueExpression': selectedItemsValueExpression
    }));
    dialog.show();
  }

  protected function getBindDisableExpression(selectedItemsValueExpression:ValueExpression):ValueExpression {
    if (!bindDisableExpression) {
      bindDisableExpression = ValueExpressionFactory.createFromFunction(function ():Boolean {
        var selection:Array = selectedItemsValueExpression.getValue();
        if (!selection || selection.length === 0) {
          return true;
        }

        var context:ConnectorContext = category.getContext();
        var targetType:ContentType = null;
        for each(var c:Content in selection) {
          //disabled if one item is not supported
          if (!context.isUploadSupported(c)) {
            return true;
          }

          //disabled when there are different content types
          if(targetType === null) {
            targetType = c.getType();
            continue;
          }

          if(targetType.getName() !== c.getType().getName()) {
            return true;
          }
        }

        return false;
      });
    }
    return bindDisableExpression;
  }
}
}
