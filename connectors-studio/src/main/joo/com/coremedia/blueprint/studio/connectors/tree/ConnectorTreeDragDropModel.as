package com.coremedia.blueprint.studio.connectors.tree {
import com.coremedia.ui.models.DragDropModel;
import com.coremedia.ui.models.TreeModel;

import ext.dd.DragSource;

public class ConnectorTreeDragDropModel implements DragDropModel {

  private var treeModel:TreeModel;

  public function ConnectorTreeDragDropModel(catalogTree:TreeModel) {
    this.treeModel = catalogTree;
  }

  public function performDefaultAction(droppedNodeIds:Array, targetNodeId:String, callback:Function = undefined):void {
  }

  public function performAlternativeAction(droppedNodeIds:Array, targetNodeId:String, callback:Function = undefined):void {
  }

  public function allowDefaultAction(source:DragSource, nodeIds:Array, targetNodeId:String):Boolean {
    return false;
  }

  public function allowAlternativeAction(source:DragSource, nodeIds:Array, targetNodeId:String):Boolean {
    return false;
  }

  public function getModelItemId():String {
    return undefined;
  }
}
}