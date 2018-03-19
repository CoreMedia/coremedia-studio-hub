package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.library.ConnectorCollectionViewExtension;
import com.coremedia.blueprint.studio.connectors.tree.ConnectorTreeDragDropModel;
import com.coremedia.blueprint.studio.connectors.tree.ConnectorTreeModel;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.editorContext;

import mx.resources.ResourceManager;

public class ConnectorsStudioPluginBase extends StudioPlugin {

  public function ConnectorsStudioPluginBase(config:StudioPlugin = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    //unfortunately we are not allowed to perform a REST request to determine which models to add
    var connectorTypes:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorTypes', 'connector_types');
    var typeNames:Array = connectorTypes.split(",");
    for each(var typeName:String in typeNames) {
      var treeModel:ConnectorTreeModel = new ConnectorTreeModel(typeName);
      addTreeModel(treeModel);
    }
  }


  private function addTreeModel(treeModel:ConnectorTreeModel):void {
    var collectionViewManagerInternal:CollectionViewManagerInternal =
            ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);
    editorContext.getCollectionViewExtender().addExtension(new ConnectorCollectionViewExtension());
    collectionViewManagerInternal.addTreeModel(treeModel, new ConnectorTreeDragDropModel(treeModel));
  }
}
}
