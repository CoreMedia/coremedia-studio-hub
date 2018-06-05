package com.coremedia.blueprint.connectors.impl;

/**
 * This model is requested once during the Studio startup
 * to determine the tree that should be rendered into the library.
 */
public class ConnectorType {
  private String name;
  private boolean rootNodeVisible = true;

  ConnectorType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isRootNodeVisible() {
    return rootNodeVisible;
  }

  public void setRootNodeVisible(boolean rootNodeVisible) {
    this.rootNodeVisible = rootNodeVisible;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof ConnectorType) {
      return ((ConnectorType)obj).name.equals(this.name);
    }
    return false;
  }
}
