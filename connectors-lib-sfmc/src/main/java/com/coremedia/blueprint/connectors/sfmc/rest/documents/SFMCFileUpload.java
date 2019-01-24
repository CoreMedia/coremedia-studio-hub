package com.coremedia.blueprint.connectors.sfmc.rest.documents;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 *
 */
public class SFMCFileUpload {

  private SFMCAssetType assetType;
  private SFMCCategory category;
  private String name;
  private String file;

  public SFMCFileUpload() {
  }

  public SFMCFileUpload(@NonNull SFMCCategory category, @NonNull String name, int assetTypeId, @NonNull String assetTypeName, @NonNull String base64EncodedFile) {
    this.category = category;

    assetType = new SFMCAssetType();
    assetType.setName(assetTypeName);
    assetType.setId(assetTypeId);

    this.name = name;
    this.file = base64EncodedFile;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }
}
