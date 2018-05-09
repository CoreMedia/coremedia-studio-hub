package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.library.ConnectorCollectionViewExtension;
import com.coremedia.blueprint.studio.connectors.model.ConnectorImpl;
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

  [Bindable]
  public var defaultColumns:String;

  private var cType:String;
  private var dColumns:String;

  public function AddConnectorPluginBase(config:AddConnectorPlugin = null) {
    this.cType = config.connectorType;
    this.dColumns = config.defaultColumns;
  }

  public function init(editorContext:IEditorContext):void {
    var treeModel:ConnectorTreeModel = new ConnectorTreeModel(cType);
    addTreeModel(treeModel);

    treeModel.getConnectorExpression().loadValue(function (c:ConnectorImpl):void {
      if (dColumns && dColumns.length > 0) {
        var cols:Array = dColumns.split(",");
        ConnectorHelper.setDefaultColumns(c.getConnectorType(), cols);
      }
    });
  }

  private function addTreeModel(treeModel:ConnectorTreeModel):void {
    var collectionViewManagerInternal:CollectionViewManagerInternal =
            ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);
    editorContext.getCollectionViewExtender().addExtension(new ConnectorCollectionViewExtension(), 600);
    collectionViewManagerInternal.addTreeModel(treeModel, new ConnectorTreeDragDropModel(treeModel));
  }
}
}
