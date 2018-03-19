package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

public class ConnectorSearchResultRepresentation {
  private final List<ConnectorEntity> hits;
  private final long total;


  @JsonCreator
  public ConnectorSearchResultRepresentation(@JsonProperty("hits") List<ConnectorEntity> hits, @JsonProperty("total")long total) {
    this.hits = hits;
    this.total = total;
  }

  @JsonSerialize
  public List<ConnectorEntity> getHits() {
    return hits;
  }

  @JsonSerialize
  public long getTotal() {
    return total;
  }
}
