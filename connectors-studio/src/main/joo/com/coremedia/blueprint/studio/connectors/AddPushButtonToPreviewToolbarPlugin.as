package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.push.PushButton;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.preview.PreviewPanelToolbar;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.skins.ButtonSkin;

import ext.Component;
import ext.Plugin;

public class AddPushButtonToPreviewToolbarPlugin implements Plugin {
  private var previewToolbar:PreviewPanelToolbar;

  public function init(component:Component):void {
    this.previewToolbar = component as PreviewPanelToolbar;
    component.addListener('afterlayout', initMenu);
  }

  private function initMenu():void {
    previewToolbar.removeListener('afterlayout', initMenu);

    var ve:ValueExpression = this.previewToolbar['getCurrentPreviewContentValueExpression']();
    var pushButton:PushButton = new PushButton(PushButton({
      'selectedItemsValueExpression': ve,
      'scale': 'medium',
      'ui': ButtonSkin.WORKAREA.getSkin()
    }));

    previewToolbar.insert(6, pushButton);
  }
}
}
