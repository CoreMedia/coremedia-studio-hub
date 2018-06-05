package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSizeEntity extends AbstractCantoEntity {

  @JsonProperty("value")
  private long value;

  @JsonProperty("displaystring")
  private String displayString;

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public String getDisplayString() {
    return displayString;
  }

  public void setDisplayString(String displayString) {
    this.displayString = displayString;
  }
}
