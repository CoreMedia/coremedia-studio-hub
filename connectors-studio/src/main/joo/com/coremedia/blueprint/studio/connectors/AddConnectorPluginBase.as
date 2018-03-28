package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.library.ConnectorCollectionViewExtension;
import com.coremedia.blueprint.studio.connectors.tree.ConnectorTreeDragDropModel;
import com.coremedia.blueprint.studio.connectors.tree.ConnectorTreeModel;
import com.coremedia.cms.editor.sdk.EditorPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;
import com.coremedia.cms.editor.sdk.editorContext;

/**
 * Plugin for adding the different connector types to the studio library.
 */
public class AddConnectorPluginBase implements EditorPlugin {

  [Bindable]
  public var connectorType:String;

  private var cType:String;

  public function AddConnectorPluginBase(config:AddConnectorPlugin = null) {
    super(config);
    this.cType = config.connectorType;
  }

  public function init(editorContext:IEditorContext):void {
    var treeModel:ConnectorTreeModel = new ConnectorTreeModel(cType);
    addTreeModel(treeModel);
  }

  private function addTreeModel(treeModel:ConnectorTreeModel):void {
    var collectionViewManagerInternal:CollectionViewManagerInternal =
            ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);
    editorContext.getCollectionViewExtender().addExtension(new ConnectorCollectionViewExtension(), 600);
    collectionViewManagerInternal.addTreeModel(treeModel, new ConnectorTreeDragDropModel(treeModel));
  }
}
}
