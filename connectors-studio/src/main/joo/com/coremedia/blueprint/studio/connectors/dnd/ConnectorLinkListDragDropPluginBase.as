package com.coremedia.blueprint.studio.connectors.dnd {
import com.coremedia.cms.editor.sdk.premular.fields.LinkListGridPanel;
import com.coremedia.cms.editor.sdk.util.ContentLinkListWrapper;

import ext.Component;
import ext.Plugin;

/**
 *
 */
public class ConnectorLinkListDragDropPluginBase implements Plugin {
  private var gridPanel:LinkListGridPanel;

  public function init(cmp:Component):void {
    gridPanel = cmp as LinkListGridPanel;
    gridPanel.addListener('afterrender', initDnD);
  }

  private function initDnD():void {
    var llWrapper:ContentLinkListWrapper = gridPanel.initialConfig.linkListWrapper as ContentLinkListWrapper;
    if(llWrapper) {
      new ConnectorLinkListDropAreaTarget(gridPanel, gridPanel.getView(), llWrapper);
    }
  }

}
}