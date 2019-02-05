package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.push.PushButton;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.preview.PreviewPanelToolbar;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Component;
import ext.Plugin;
import ext.toolbar.Separator;

public class AddPushButtonToPreviewToolbarPlugin implements Plugin {
  private var previewToolbar:PreviewPanelToolbar;

  public function init(component:Component):void {
    this.previewToolbar = component as PreviewPanelToolbar;
    component.addListener('afterlayout', initMenu);
  }

  private function initMenu():void {
    previewToolbar.removeListener('afterlayout', initMenu);

    var c:Content = this.previewToolbar['getCurrentPreviewContentValueExpression']().getValue();
    var ve:ValueExpression = ValueExpressionFactory.createFromValue([c]);
    var pushButton:PushButton = new PushButton(PushButton({
      'selectedItemsValueExpression': ve,
      'scale' : 'medium'
    }));

    previewToolbar.add(new Separator());
    previewToolbar.add(pushButton);
  }
}
}
