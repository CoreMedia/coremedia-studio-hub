package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorItemTypes;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains all information about the item documents types.
 */
public class ConnectorItemTypesImpl implements ConnectorItemTypes {
  private static final String SETTINGS = "settings";
  private Map<String, Object> types;

  ConnectorItemTypesImpl(Content itemTypes) {
    Struct settings = itemTypes.getStruct(SETTINGS);
    types = settings.toNestedMaps();
  }

  @Nonnull
  @Override
  public Map<String, Object> getTypes() {
    return types;
  }

  @Nullable
  @Override
  public String getTypeForName(String name) {
    String indicator = FilenameUtils.getExtension(name.toLowerCase());
    if(StringUtils.isEmpty(indicator)) {
      indicator = name;
    }
    Set<Map.Entry<String, Object>> entries = types.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String[] split = ((String) entry.getValue()).split(",");
      List<String> fileEndings = Arrays.asList(split);
      if (fileEndings.contains(indicator)) {
        return entry.getKey();
      }
    }

    return null;
  }

  @Nonnull
  @Override
  public List<String> getTypes(String type) {
    Set<Map.Entry<String, Object>> entries = types.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String[] split = entry.getKey().split("/");
      List<String> fileEndings = Arrays.asList(split);
      if (fileEndings.contains(type)) {
        String value = (String) entry.getValue();
        return new ArrayList<>(Arrays.asList(value.split(",")));
      }
    }
    return Collections.emptyList();
  }
}
