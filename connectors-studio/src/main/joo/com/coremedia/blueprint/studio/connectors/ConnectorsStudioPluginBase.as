package com.coremedia.blueprint.studio.connectors {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.library.ConnectorCollectionViewExtension;
import com.coremedia.blueprint.studio.connectors.library.ConnectorThumbnailResolver;
import com.coremedia.blueprint.studio.connectors.model.ConnectorPropertyNames;
import com.coremedia.blueprint.studio.connectors.tree.ConnectorTreeDragDropModel;
import com.coremedia.blueprint.studio.connectors.tree.ConnectorTreeModel;
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewManagerInternal;

public class ConnectorsStudioPluginBase extends StudioPlugin {

  public function ConnectorsStudioPluginBase(config:StudioPlugin = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);

    editorContext.registerThumbnailResolver(new ConnectorThumbnailResolver(ConnectorPropertyNames.TYPE_CONNECTOR));
    editorContext.registerThumbnailResolver(new ConnectorThumbnailResolver(ConnectorPropertyNames.TYPE_CONNECTOR_CATEGORY));
    editorContext.registerThumbnailResolver(new ConnectorThumbnailResolver(ConnectorPropertyNames.TYPE_CONNECTOR_ITEM));

    ConnectorHelper.getConnectorTypesExpression().loadValue(function(connectorTypes:Array):void {
      for each(var cType:String in connectorTypes) {
        var treeModel:ConnectorTreeModel = new ConnectorTreeModel(cType);

        var collectionViewManagerInternal:CollectionViewManagerInternal =
                ((editorContext.getCollectionViewManager()) as CollectionViewManagerInternal);
        editorContext.getCollectionViewExtender().addExtension(new ConnectorCollectionViewExtension(), 600);
        collectionViewManagerInternal.addTreeModel(treeModel, new ConnectorTreeDragDropModel(treeModel));
      }
    });
  }
}
}
