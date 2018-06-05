package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CantoCatalogsSearchResultEntity extends AbstractCantoEntity {

  @JsonProperty("catalogs")
  private List<CantoCatalogEntity> catalogs;

  public List<CantoCatalogEntity> getCatalogs() {
    return catalogs;
  }

  public void setCatalogs(List<CantoCatalogEntity> catalogs) {
    this.catalogs = catalogs;
  }
}
