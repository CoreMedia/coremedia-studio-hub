package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.cap.common.CapType;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;

public class ConnectorContext {

  private var values:Object;

  public function ConnectorContext(values:Object) {
    this.values = values;
  }

  public function isMarkAsReadEnabled():Boolean {
    return values.markAsRead;
  }

  public function getContentScope():String {
    return values.contentScope;
  }

  public function getDateFormat():String {
    return values.dateFormat || "long";
  }

  public function getDefaultColumns():Array {
    return values.defaultColumns;
  }

  public function isColumnHidden(columnName:String):Boolean {
    var columns:Array = getDefaultColumns();
    if (!columns || columns.length === 0) {
      return false;
    }

    return columns.indexOf(columnName) === -1;
  }

  public function isValidDrop(content:Content):Boolean {
    var cType:CapType = content.getType();

    var onWhitelist:Boolean = false;
    for each(var whitelistType:String in values.contentUploadTypes) {
      if(cType.isSubtypeOf(whitelistType)) {
        onWhitelist = true;
      }
    }

    return onWhitelist;
  }
}
}