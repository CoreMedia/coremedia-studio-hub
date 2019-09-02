package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;

import java.util.List;

public class ConnectorSearchResultRepresentation {
  private List<ConnectorEntity> hits;
  private long total;

  public ConnectorSearchResultRepresentation(List<ConnectorEntity> hits, long total) {
    this.hits = hits;
    this.total = total;
  }

  public List<ConnectorEntity> getHits() {
    return hits;
  }

  public long getTotal() {
    return total;
  }
}
