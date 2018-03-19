package com.coremedia.blueprint.studio.connectors.tree {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.ui.data.AbstractTreeRelation;

public class ConnectorCategoryTreeRelationImpl extends AbstractTreeRelation {
  public function ConnectorCategoryTreeRelationImpl() {
  }

  override public function getChildrenOf(node:Object):Array {
    var category:ConnectorCategory = node as ConnectorCategory;
    if (!category) {
      return undefined;
    } else {
      return category.getSubCategories();
    }
  }

  override public function getParentUnchecked(node:Object):Object {
    var category:ConnectorCategory = node as ConnectorCategory;
    if (!category) {
      return undefined;
    } else {
      return category.getParent();
    }
  }
}
}
