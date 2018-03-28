package com.coremedia.blueprint.studio.connectors {
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;

public class ConnectorsStudioPluginBase extends StudioPlugin {

  public function ConnectorsStudioPluginBase(config:StudioPlugin = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);
  }
}
}
