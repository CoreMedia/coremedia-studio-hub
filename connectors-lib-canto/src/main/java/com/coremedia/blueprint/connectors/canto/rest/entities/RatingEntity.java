package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingEntity extends AbstractCantoEntity {

  @JsonProperty("id")
  private int id;

  @JsonProperty("displaystring")
  private String displayString;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDisplayString() {
    return displayString;
  }

  public void setDisplayString(String displayString) {
    this.displayString = displayString;
  }
}

