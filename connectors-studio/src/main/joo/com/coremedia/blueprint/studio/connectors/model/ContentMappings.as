package com.coremedia.blueprint.studio.connectors.model {
public class ContentMappings {
  private var mappings:Object;

  public function ContentMappings(mappings:Object):void {
    this.mappings = mappings;
  }

  public function getMapping(name:String):String {
    var targetType:String = mappings[name];
    if(targetType) {
      return targetType;
    }
    return getDefaultMapping();
  }

  public function getDefaultMapping():String {
    return mappings[ConnectorPropertyNames.DEFAULT_MAPPING];
  }
}
}