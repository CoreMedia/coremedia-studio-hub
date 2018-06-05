package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResultEntity extends AbstractCantoEntity {

  @JsonProperty("totalcount")
  private int totalCount;

  @JsonProperty("ids")
  private int[] ids;

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public int[] getIds() {
    return ids;
  }

  public void setIds(int[] ids) {
    this.ids = ids;
  }

}
