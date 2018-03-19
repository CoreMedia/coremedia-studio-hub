package com.coremedia.blueprint.studio.connectors.rest.representation;

public class ConnectorSuggestionRepresentation {
  private String value;

  private long count;

  public ConnectorSuggestionRepresentation(String value, long count) {
    if (value == null) {
      throw new IllegalArgumentException("parameter is null: value");
    } else {
      this.value = value;
      this.count = count;
    }
  }

  public String getValue() {
    return this.value;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public long getCount() {
    return this.count;
  }
}
