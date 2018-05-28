package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.cms.editor.sdk.util.ThumbnailResolver;

public class ConnectorThumbnailResolver implements ThumbnailResolver {

  private var docType:String;

  public function ConnectorThumbnailResolver(docType:String):void {
    this.docType = docType;
  }

  public function getContentType():String {
    return docType;
  }

  public function getThumbnail(model:Object, operations:String = null):Object {
    var url:String = (model as ConnectorEntity).getThumbnailUrl();
    if (url) {
      return url;
    }
    return null;
  }
}
}