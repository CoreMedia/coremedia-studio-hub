package com.coremedia.blueprint.connectors.previewconverters;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Contains the preview data and metadata that is used for rendering.
 */
public class PreviewConversionResult {
  private Map<String,Object> metaData;
  private String result;

  PreviewConversionResult(String result) {
    this(result, Collections.emptyMap());
  }

  PreviewConversionResult(String result, Map<String,Object> metaData) {
    this.result = result;
    this.metaData = metaData;
  }

  @Nonnull
  public Map<String, Object> getMetaData() {
    return metaData;
  }

  public String getResult() {
    return result;
  }
}
