package com.coremedia.blueprint.connectors.s7;

import com.scene7.ipsapi.xsd._2013_02_15.Asset;
import com.scene7.ipsapi.xsd._2013_02_15.Folder;

/**
 *
 */
public class S7Container {

  private Folder folder;
  private Asset asset;

  public Folder getFolder() {
    return folder;
  }

  public void setFolder(Folder folder) {
    this.folder = folder;
  }

  public Asset getAsset() {
    return asset;
  }

  public void setAsset(Asset asset) {
    this.asset = asset;
  }
}
