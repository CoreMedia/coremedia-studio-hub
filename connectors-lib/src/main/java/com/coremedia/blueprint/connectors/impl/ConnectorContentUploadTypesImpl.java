package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorContentUploadTypes;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectorContentUploadTypesImpl implements ConnectorContentUploadTypes {
  private Map<String, Object> properties;

  ConnectorContentUploadTypesImpl(Struct settings) {
    properties = settings.getProperties();
  }

  @NonNull
  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @NonNull
  @Override
  public List<String> getBlobPropertyNames(@NonNull ContentType contentType) {
    List<String> propertyNames = new ArrayList<>();
    ContentType type = contentType;
    while(type != null) {
      String value = (String) properties.getOrDefault(type.getName(), null);
      if(value != null) {
        propertyNames.add(value);
      }

      type = type.getParent();
    }

    return propertyNames;
  }
}
