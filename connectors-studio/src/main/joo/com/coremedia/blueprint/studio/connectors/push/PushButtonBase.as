package com.coremedia.blueprint.studio.connectors.push {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategoryImpl;
import com.coremedia.ui.components.IconButton;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class PushButtonBase extends IconButton {
  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  public function PushButtonBase(config:PushButton = null) {
    super(config);
  }

  protected function getPushableConnectionsExpression():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var rootCategories:Array = ConnectorHelper.getTopLevelCategoriesExpression().getValue();
      if (rootCategories === undefined) {
        return undefined;
      }

      var result:Array = [];
      for each(var root:ConnectorCategoryImpl in rootCategories) {
        if (root.getConnector() != null && root.getContext().isContentUploadSupported()) {
          result.push(root);
        }
      }

      setDisabled(false);
      return result;
    });
  }
}
}
