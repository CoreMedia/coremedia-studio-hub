package com.coremedia.blueprint.studio.connectors.push {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.cap.undoc.content.Content;
import com.coremedia.ui.components.IconButton;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class PushButtonBase extends IconButton {
  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  public var disabledExpression:ValueExpression;

  public function PushButtonBase(config:PushButton = null) {
    super(config);
  }

  override protected function afterRender():void {
    super.afterRender();

    ConnectorHelper.getPushableConnectionsExpression().loadValue(function (conns:Array):void {
      setDisabled(conns.length === 0);
    });
  }

  protected function getDisabledExpression(selectionExpression:ValueExpression):ValueExpression {
    if (!disabledExpression) {
      disabledExpression = ValueExpressionFactory.createFromFunction(function ():Boolean {
        var selection:Array = selectionExpression.getValue();
        if (!selection || selection.length === 0) {
          return true;
        }

        for each(var entity:Object in selection) {
          if (!(entity is Content)) {
            return true;
          }
        }
        return false;
      });
    }
    return disabledExpression;
  }
}
}
