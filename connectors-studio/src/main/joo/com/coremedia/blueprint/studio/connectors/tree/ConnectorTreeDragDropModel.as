package com.coremedia.blueprint.studio.connectors.tree {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorContext;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cap.content.Content;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.models.DragDropModel;
import com.coremedia.ui.models.TreeModel;

import ext.dd.DragSource;

public class ConnectorTreeDragDropModel implements DragDropModel {

  private var treeModel:TreeModel;

  public function ConnectorTreeDragDropModel(catalogTree:ConnectorTreeModel) {
    this.treeModel = catalogTree;
  }

  public function performDefaultAction(droppedNodeIds:Array, targetNodeId:String, callback:Function = undefined):void {
    var connectorCategory:ConnectorCategory= beanFactory.getRemoteBean(targetNodeId) as ConnectorCategory;
    if(connectorCategory) {
      connectorCategory.dropContents(getContents(droppedNodeIds), true, callback);
    }
  }

  public function performAlternativeAction(droppedNodeIds:Array, targetNodeId:String, callback:Function = undefined):void {
    var connectorCategory:ConnectorCategory= beanFactory.getRemoteBean(targetNodeId) as ConnectorCategory;
    if(connectorCategory) {
      connectorCategory.dropContents(getContents(droppedNodeIds), false, callback);
    }
  }

  public function allowDefaultAction(source:DragSource, nodeIds:Array, targetNodeId:String):Boolean {
    if (getContents(nodeIds).length === 0) {
      return false;
    }

    if (!isContentUploadEnabled(targetNodeId, getContents(nodeIds))) {
      return false;
    }
    return true;
  }

  public function allowAlternativeAction(source:DragSource, nodeIds:Array, targetNodeId:String):Boolean {
    if (getContents(nodeIds).length === 0) {
      return false;
    }

    if (!isContentUploadEnabled(targetNodeId, getContents(nodeIds))) {
      return false;
    }
    return true;
  }

  public function getModelItemId():String {
    return undefined;
  }

  /**
   * Checks if the drop is enabled for the given target connector node.
   */
  private function isContentUploadEnabled(targetNodeId:String, contents:Array):Boolean {
    var connectorObject:ConnectorObject = beanFactory.getRemoteBean(targetNodeId) as ConnectorObject;
    if (!connectorObject) {
      return false;
    }

    var category:ConnectorCategory = connectorObject as ConnectorCategory;
    if (!category || !category.isContentUploadEnabled()) {
      return false;
    }

    var connectionId:String = category.getConnectionId();
    var context:ConnectorContext = category.getConnector().getConnection(connectionId).getContext();
    for each(var c:Content in contents) {
      if(!context.isValidDrop(c)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Converts the list of bean ids to an array of content.
   * @param beanIds the ids to retrieve the content for.
   * @return a content array
   */
  private function getContents(beanIds:Array):Array {
    var contents:Array = [];
    for (var i:int = 0; i < beanIds.length; i++) {
      var id:String = beanIds[i];
      if (id.indexOf('content/') === -1) {
        return [];
      }
      var content:Content = beanFactory.getRemoteBean(id) as Content;
      if (content) {
        contents.push(content);
      }
    }
    return contents;
  }
}
}