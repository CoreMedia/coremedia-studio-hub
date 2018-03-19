package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorPreviewTemplates;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;

import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 */
public class ConnectorPreviewTemplatesImpl implements ConnectorPreviewTemplates {
  private static final String SETTINGS = "settings";
  private Map<String, Object> templates;

  ConnectorPreviewTemplatesImpl(Content previewTemplates) {
    Struct settings = previewTemplates.getStruct(SETTINGS);
    templates = settings.toNestedMaps();
  }

  @Nullable
  @Override
  public String getTemplate(String itemType) {
    if(templates.containsKey(itemType)) {
      return (String) templates.get(itemType);
    }
    return null;
  }
}
