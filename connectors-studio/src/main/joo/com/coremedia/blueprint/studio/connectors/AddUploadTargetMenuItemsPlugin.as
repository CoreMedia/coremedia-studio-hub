package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.push.PushTargetMenuItem;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.preview.PreviewPanelToolbar;
import com.coremedia.cms.editor.sdk.preview.PreviewPanelToolbarBase;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Component;
import ext.Plugin;
import ext.button.Button;
import ext.menu.Menu;
import ext.menu.Separator;

public class AddUploadTargetMenuItemsPlugin implements Plugin {
  private var previewToolbar:PreviewPanelToolbar;

  public function init(component:Component):void {
    this.previewToolbar = component as PreviewPanelToolbar;
    component.addListener('afterlayout', initMenu);
  }

  private function initMenu():void {
    previewToolbar.removeListener('afterlayout', initMenu);

    var targetMenuButton:Button = previewToolbar.queryById(PreviewPanelToolbarBase.OPEN_IN_BROWSER_BUTTON_ITEM_ID) as Button;
    var targetMenu:Menu = targetMenuButton.getMenu();
    targetMenu.add(new Separator());

    var c:Content = this.previewToolbar['getCurrentPreviewContentValueExpression']().getValue();
    var ve:ValueExpression = ValueExpressionFactory.createFromValue([c]);

    ConnectorHelper.getPushableConnectionsExpression().loadValue(function (categories:Array):void {
      for each(var category in categories) {
        var mItem:PushTargetMenuItem = new PushTargetMenuItem(PushTargetMenuItem(
                {
                  'selectedItemsValueExpression': ve,
                  'category': category
                })
        );
        targetMenu.add(mItem);
      }
    });
  }
}
}
