package com.coremedia.blueprint.studio.connectors.rest.representation;

public class ConnectorChildRepresentation {
  private String displayName;
  private Object child;

  public ConnectorChildRepresentation(){
  }

  public ConnectorChildRepresentation(String displayName, Object child) {
    this.displayName = displayName;
    this.child = child;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Object getChild() {
    return child;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setChild(Object child) {
    this.child = child;
  }
}