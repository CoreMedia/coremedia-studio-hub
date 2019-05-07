package com.coremedia.blueprint.studio.connectors.push {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorContext;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.components.StudioDialog;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;

public class PushDialogBase extends StudioDialog {

  [Bindable]
  public var rootCategory:ConnectorCategory;

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  private var categorySelectionExpression:ValueExpression;
  private var propertyNamesExpression:ValueExpression;

  public function PushDialogBase(config:PushDialogBase = null) {
    super(config);
  }

  protected function getSelectedCategoryExpression(rootCategory:ConnectorCategory):ValueExpression {
    if (!categorySelectionExpression) {
      this.categorySelectionExpression = ValueExpressionFactory.createFromValue(rootCategory);
    }
    return this.categorySelectionExpression;
  }

  protected function getTargetContentType(selectionExpression:ValueExpression):String {
    var value:* = selectionExpression.getValue();
    var c:Content = value[0];
    if(value is Content){
      c = value;
    }
    return c.getType().getName();
  }

  protected function getPropertyNamesExpression(rootCategory:ConnectorCategory, selectionExpression:ValueExpression):ValueExpression {
    var value:* = selectionExpression.getValue();
    var c:Content = value[0];
    if(value is Content){
      c = value;
    }

    var ctx:ConnectorContext = rootCategory.getContext();
    var propertyNames:Array = ctx.getUploadPropertyNames(c);
    return ValueExpressionFactory.createFromValue(propertyNames);
  }

  protected function getSelectedPropertyNamesExpression():ValueExpression {
    if(!propertyNamesExpression) {
      propertyNamesExpression = ValueExpressionFactory.create('values', beanFactory.createLocalBean());
    }
    return propertyNamesExpression;
  }

  protected function okPressed():void {
    var category:ConnectorCategory = categorySelectionExpression.getValue();
    var items:* = selectedItemsValueExpression.getValue();
    if(items is Content) {
      items = [items];
    }

    var propertyNames:Array = [];
    var selection:Object = getSelectedPropertyNamesExpression().getValue().toObject();
    for(var propertyName:String in selection) {
      var selected:Boolean = selection[propertyName];
      if(selected) {
        propertyNames.push(propertyName);
      }
    }
    category.uploadContents(items, propertyNames,true);
    this.close();
  }
}
}
