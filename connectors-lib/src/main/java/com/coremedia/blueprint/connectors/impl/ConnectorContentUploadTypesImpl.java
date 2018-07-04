package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorContentUploadTypes;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class ConnectorContentUploadTypesImpl implements ConnectorContentUploadTypes {
  private static final String SETTINGS = "settings";
  private static final String TYPES = "types";
  private Map<String, Object> properties;

  ConnectorContentUploadTypesImpl(Content content) {
    Struct settings = content.getStruct(SETTINGS).getStruct(TYPES);
    properties = settings.getProperties();
  }

  @Nonnull
  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Nullable
  @Override
  public String getContentProperty(ContentType contentType) {
    Set<Map.Entry<String, Object>> entries = properties.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String key = entry.getKey();
      if(contentType.isSubtypeOf(key)) {
        return (String) entry.getValue();
      }
    }

    return null;
  }

}
