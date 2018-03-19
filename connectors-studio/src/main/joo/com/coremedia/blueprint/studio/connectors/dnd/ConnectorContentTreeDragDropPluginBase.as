package com.coremedia.blueprint.studio.connectors.dnd {
import com.coremedia.cms.editor.sdk.collectionview.tree.LibraryTree;

import ext.Component;
import ext.Plugin;

/**
 * Add connector DnD support for the library tree
 */
public class ConnectorContentTreeDragDropPluginBase implements Plugin {
  private var treePanel:LibraryTree;
  private var folderBlackListCsv:String;

  public function ConnectorContentTreeDragDropPluginBase(config:ConnectorContentTreeDragDropPlugin = null){
    folderBlackListCsv = config.folderBlacklist || "";
  }

  public function init(cmp:Component):void {
    treePanel = cmp as LibraryTree;
    treePanel.addListener('afterrender', initDnD);
  }

  private function initDnD():void {
    new ConnectorContentTreeDropAreaTarget(treePanel, treePanel.getView(), folderBlackListCsv.split(","));
  }
}
}