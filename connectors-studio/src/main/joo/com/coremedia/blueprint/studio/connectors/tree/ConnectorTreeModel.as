package com.coremedia.blueprint.studio.connectors.tree {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.Connector;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategoryImpl;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundChildTreeModel;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.models.NodeChildren;

import mx.resources.ResourceManager;

public class ConnectorTreeModel implements CompoundChildTreeModel {
  private var enabled:Boolean = true;
  public static const ID_PREFIX:String = "connector/";
  public static const CONNECTOR_TREE_ID:String = "connectorTreeId";
  private var connectorExpression:ValueExpression;
  private var rootNodeVisible:Boolean;

  public function ConnectorTreeModel(connectorType:String, rootNodeVisible:Boolean) {
    this.rootNodeVisible = rootNodeVisible;
    this.connectorExpression = ConnectorHelper.getConnectorExpression(connectorType);
  }

  public function getConnectorExpression():ValueExpression {
    return connectorExpression;
  }

  public function setEnabled(enabled:Boolean):void {
    this.enabled = enabled;
  }

  public function isEnabled():Boolean {
    return enabled;
  }

  public function isEditable(model:Object):Boolean {
    return false;
  }

  public function rename(model:Object, newName:String, oldName:String, callback:Function):void {
  }

  public function isRootVisible():Boolean {
    return rootNodeVisible;
  }

  public function getRootId():String {
    return getNodeId(getConnector());
  }

  public function getText(nodeId:String):String {
    if (!getConnector()) {
      return undefined
    }

    if (isConnectorId(nodeId)) {
      return computeStoreText();
    } else {
      var node:RemoteBean = getNodeModel(nodeId) as RemoteBean;
      if (node is ConnectorCategory) {
        return ConnectorCategory(node).getDisplayName();
      }
      else if (node is ConnectorItem) {
        return ConnectorItem(node).getDisplayName();
      }
    }

    return undefined;
  }

  private function getCategoryName(node:RemoteBean):String {
    return ConnectorCategory(node).getDisplayName();
  }

  public function getIconCls(nodeId:String):String {
    return computeIconCls(nodeId, undefined);
  }

  public function getChildren(nodeId:String):NodeChildren {
    if (!getConnector()) {
      return undefined
    }

    if (isConnectorId(nodeId)) {
      var connector:Connector = getNodeModel(nodeId) as Connector;
      return getChildrenFor(connector.getRootCategories(), connector.getChildrenByName(), ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Category_icon'));
    }
    var category:ConnectorCategory = getNodeModel(nodeId) as ConnectorCategory;
    var subCategories:Array = connectorCategoryTreeRelation.getChildrenOf(category);

    if (!subCategories) {
      return undefined;
    }
    //we would like to sort the sub categories by display names.
    //but before that we have to make sure that all sub categories are loaded.
    if (subCategories.length > 0) {
      if (!preloadChildren(subCategories)) {
        return undefined;
      }
      //don't change the original list of sub categories.
      subCategories = subCategories.slice();
    }

    return getChildrenFor(subCategories, category.getChildrenByName(), ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Category_icon'));
  }

  /**
   * We children are preloaded, this fixes the problem that raises for breadcrumbs:
   * If you select a leaf category the first time in the search mode, the node is not
   * found in the tree since it has been not loaded yet.
   * As a result, the BindTreeSelectionPlugin selected the default node, which is the content root.
   * @param subCategories
   * @return true if all children are loaded
   */
  private function preloadChildren(subCategories:Array):Boolean {
    return subCategories.every(function (subCategory:ConnectorCategory):Boolean {
      subCategory.load();
      return subCategory.isLoaded();
    });
  }

  protected function getChildrenFor(children:Array, childrenByName:Object, iconCls:String):NodeChildren {
    if (!children) {
      return undefined;
    }
    if (!childrenByName) {
      return undefined;
    }

    var nameByChildId:Object = computeNameByChildId(childrenByName);
    var childIds:Array = [];
    var namesById:Object = {};
    var iconById:Object = {};
    var clsByChildId:Object = computeTextClsByChildId(childrenByName);
    for (var i:uint = 0; i < children.length; i++) {
      var childId:String = getNodeId(children[i]);
      childIds.push(childId);
      namesById[childId] = nameByChildId[childId];
      iconById[childId] = computeIconCls(childId, iconCls);
      clsByChildId[childIds] = clsByChildId[childId];
    }
    return new NodeChildren(childIds, namesById, iconById, clsByChildId);
  }

  private function computeIconCls(childId:String, defaultIconCls:String):String {
    var child:RemoteBean = beanFactory.getRemoteBean(childId);
    var icon:String = ConnectorHelper.getTypeCls(child as ConnectorObject);
    if (icon) {
      return icon;
    }
    return defaultIconCls;
  }

  private function computeNameByChildId(childrenByIds:Object):Object {
    var nameByUriPath:Object = {};
    for (var childId:String in childrenByIds) {
      var child:ConnectorObject = childrenByIds[childId].child as ConnectorObject;
      if (child is ConnectorCategory) {
        nameByUriPath[getNodeId(child)] = getCategoryName(child);
      }
      else if (child) {
        nameByUriPath[getNodeId(child)] = childrenByIds[childId].displayName;
      }
    }
    return nameByUriPath;
  }

  private function computeTextClsByChildId(childrenByIds:Object):Object {
    var nameByUriPath:Object = {};
    for (var childId:String in childrenByIds) {
      var child:ConnectorObject = childrenByIds[childId].child as ConnectorObject;
      nameByUriPath[getNodeId(child)] = child.getTextCls();
    }
    return nameByUriPath;
  }

  /**
   * Creates an array that contains the tree path for the node with the given id.
   * @param nodeId The id to build the path for.
   * @return
   */
  public function getIdPath(nodeId:String):Array {
    if (!getConnector()) {
      return undefined
    }
    return getIdPathFromModel(getNodeModel(nodeId));
  }

  public function getIdPathFromModel(model:Object):Array {
    if (!(model is ConnectorObject)) {
      return null;
    }
    if (!getConnector()) {
      return undefined
    }

    if (getConnector().getConnectorType() !== (model as ConnectorObject).getConnector().getConnectorType()) {
      return null;
    }

    var path:Array = [];
    var node:RemoteBean = model as RemoteBean;
    var treeNode:RemoteBean;
    if (node is ConnectorItem) {
      treeNode = ConnectorItem(node).getParent();
      if (!treeNode) {
        trace('[ERROR]', 'Parent not set for connector item ' + node.getUriPath());
      }
    } else {
      treeNode = node;
    }

    var category:ConnectorCategory = treeNode as ConnectorCategory;
    if (category) {
      //we have to reverse the path to root as we want from the root.
      var pathToRoot:Array = connectorCategoryTreeRelation.pathToRoot(treeNode);
      if (pathToRoot === undefined) {
        return undefined;
      } else if (!pathToRoot) {
        return null;
      }
      path = pathToRoot.reverse();
      //path contains the root category at top. so we need the store above it. and not catalog or something
      treeNode = getConnector();
    }
    path.unshift(treeNode);
    //add the store as top node if not happened already
    if (treeNode !== getConnector()) {
      path.unshift(getConnector());
    }
    return path.map(getNodeId);
  }

  private function getConnector():Connector {
    var connector:Connector = connectorExpression.getValue();
    if (!connector.isLoaded()) {
      connector.load();
      return undefined;
    }
    var rootCategories:Array = connector.getRootCategories();
    //pre-load root nodes since the intermediate notes are hidden, see README.md
    for each(var root:ConnectorCategoryImpl in rootCategories) {
      if (!root.isLoaded()) {
        root.load();
        return undefined;
      }
    }

    return connector;
  }

  private function computeStoreText():String {
    var connectorName:String = getConnector().getName();
    if (!connectorName) {
      connectorName = ConnectorHelper.getTypeLabel(getConnector());
    }
    return connectorName;
  }

  public function getNodeId(model:Object):String {
    var bean:RemoteBean = (model as RemoteBean);
    if (!bean || !(bean is ConnectorObject)) {
      return null;
    }

    if (!getConnector()) {
      return undefined;
    }

    if (getConnector().getRootCategories() && getConnector().getRootCategories().length === 0) {
      return null;
    }

    return bean.getUriPath();
  }

  public function getNodeModel(nodeId:String):Object {
    if (nodeId.indexOf(ID_PREFIX) !== 0) {
      return null;
    }
    var connectorObject:ConnectorObject = beanFactory.getRemoteBean(nodeId) as ConnectorObject;
    if (!connectorObject) {
      return null;
    }

    //must not have been loaded yet since the type is an immediate property
    var thisConnector:Connector = getConnectorExpression().getValue();
    if(!thisConnector) {
      return null;
    }

    if (!connectorObject.getConnector()) {
      return null;
    }

    if (thisConnector.getConnectorType() !== connectorObject.getConnector().getConnectorType()) {
      return null;
    }

    return connectorObject;
  }


  public function toString():String {
    return ID_PREFIX;
  }

  public function getTreeId():String {
    return CONNECTOR_TREE_ID;
  }

  private function isConnectorId(id:String):Boolean {
    return getConnector().getUriPath() === id;
  }

  public function getTextCls(nodeId:String):String {
    return ".x-slider.x-disabled";
  }
}
}
