package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorContentUploadTypes;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
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
  public List<String> getPropertyNames(@NonNull ContentType contentType) {
    List<String> propertyNames = new ArrayList<>();
    ContentType type = contentType;
    while (type != null) {
      String value = (String) properties.getOrDefault(type.getName(), null);
      addNames(propertyNames, value);
      type = type.getParent();
    }

    return propertyNames;
  }

  @NonNull
  @Override
  public List<String> getPropertyNames(@NonNull String contentType) {
    List<String> propertyNames = new ArrayList<>();
    String value = (String) properties.getOrDefault(contentType, null);
    addNames(propertyNames, value);
    return propertyNames;
  }

  private void addNames(List<String> propertyNames, String value) {
    if (value != null) {
      if (value.contains(",")) {
        String[] split = value.split(",");
        List<String> names = Arrays.asList(split);
        for (String name : names) {
          if (!propertyNames.contains(name)) {
            propertyNames.add(name);
          }
        }
      }
      else {
        if(!propertyNames.contains(value)) {
          propertyNames.add(value);
        }
      }
    }
  }

}
