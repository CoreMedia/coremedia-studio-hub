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

  public function getUploadPropertyNames(content:Content):Array {
    if(!values.contentUploadTypes) {
      return [];
    }

    var cType:CapType = content.getType();
    var propertyNames:Array = [];
    for (var m:String in values.contentUploadTypes) {
      if(cType.isSubtypeOf(m)) {
        var cTypes:Array = values.contentUploadTypes[m];
        for each(var cTypeName:String in cTypes) {
          if(propertyNames.indexOf(cTypeName) === -1) {
            propertyNames.push(cTypeName);
          }
        }
      }
    }

    return propertyNames;
  }

  public function isUploadSupported(content:Content):Boolean {
    if(!values.contentUploadTypes) {
      return false;
    }

    var cType:CapType = content.getType();

    var onWhitelist:Boolean = false;
    if(values.contentUploadTypes) {
      for (var m:String in values.contentUploadTypes) {
        if(cType.isSubtypeOf(m)) {
          onWhitelist = true;
        }
      }
    }

    return onWhitelist;
  }

  public function isContentUploadSupported():Boolean {
    if(values.contentUploadTypes) {
      for (var m:String in values.contentUploadTypes) {
        return true;
      }
    }

    return false;
  }
}
}
