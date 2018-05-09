package com.coremedia.blueprint.studio.connectors.helper {
import com.coremedia.blueprint.base.components.quickcreate.processing.ProcessingData;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ContentMappings;
import com.coremedia.blueprint.studio.connectors.service.ConnectorContentService;
import com.coremedia.cap.content.Content;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

public class ContentCreationHelper {
  private static const SELECTION:String = "connectorItem";

  /**
   * Used from quick create dialog post processing
   * @param content the content that has been created
   * @param data the quick create data
   * @param callback the callback to call when finished
   */
  public static function postProcessExternalContent(content:Content,
                                                    data:ProcessingData,
                                                    callback:Function):void {
    var item:ConnectorItem = data.get(SELECTION);
    ConnectorContentService.processContent(content, item, callback);
  }

  /**
   * Used to calculate the content name of the quick create dialog
   * @param selectedItemsValueExpression the current selection the dialog is called for
   */
  public static function getDefaultNameExpression(selectedItemsValueExpression:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      var selection:Array = selectedItemsValueExpression.getValue();
      if (selection && selection.length > 0) {
        var item:ConnectorItem = selection[0] as ConnectorItem;
        if (item) {
          var name:String = item.getDisplayName();
          if(name.indexOf(".") > 0) {
            name = name.substr(0, name.indexOf("."));
          }
          return name;
        }
      }
      return null;
    });
  }

  /**
   * Calculates the target content type using the configured content mapping
   * @param selectedItemsValueExpression the current selection the dialog is called for
   */
  public static function getContentTypeExpression(selectedItemsValueExpression:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():String {
      var selection:Array = selectedItemsValueExpression.getValue();
      if (selection && selection.length > 0) {
        var item:ConnectorItem = selection[0] as ConnectorItem;
        if (item) {
          return item.getTargetContentType();
        }
      }
      return null;
    });
  }

  /**
   * Enables the quick create button only for connector items
   * @param selections the current selection
   */
  public static function disableCreateButton(selections:Array):Boolean {
    if (!selections) {
      return true;
    }

    if(selections.length > 1) {
      return true;
    }

    var selection:* = selections[0];
    return (!(selection && selection is ConnectorItem));
  }

  /**
   * We use a custom processing data factory method here to apply the current selection
   * to the model Therefore we ca re-use it for the postprocessing where the actual content is applied.
   * @param selectedItemsValueExpression the current selection
   */
  public static function getProcessingDataFactory(selectedItemsValueExpression:ValueExpression):Function {
    return function (contentType:String, bindTo:ValueExpression):ProcessingData {
      var data:ProcessingData = new ProcessingData();
      data.set(SELECTION, selectedItemsValueExpression.getValue()[0]);
      return data;
    }

  }
}
}