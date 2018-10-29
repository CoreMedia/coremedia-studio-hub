package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorContentUploadTypes;
import com.coremedia.cap.struct.Struct;
import edu.umd.cs.findbugs.annotations.NonNull;

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
}
