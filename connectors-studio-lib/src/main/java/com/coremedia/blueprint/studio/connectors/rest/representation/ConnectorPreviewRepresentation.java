package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation used when a preview is generated
 */
public class ConnectorPreviewRepresentation {
  private String html;
  private Map<String,Object> metaData = new HashMap<>();

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  public Map<String,Object> getMetaData() {
    return metaData;
  }

  public void addMetaData(ConnectorMetaData mt) {
    if(mt != null) {
      this.metaData.putAll(mt.getMetadata());
    }
  }
}
