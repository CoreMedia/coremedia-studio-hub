package com.coremedia.blueprint.connectors.celum.rest;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class VersionInformation {

  private int versionId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date assetVersionDate;

  private int versionedBy;

  private boolean isActiveVersion;

  public int getVersionId() {
    return versionId;
  }

  public void setVersionId(int versionId) {
    this.versionId = versionId;
  }

  public Date getAssetVersionDate() {
    return assetVersionDate;
  }

  public void setAssetVersionDate(Date assetVersionDate) {
    this.assetVersionDate = assetVersionDate;
  }

  public int getVersionedBy() {
    return versionedBy;
  }

  public void setVersionedBy(int versionedBy) {
    this.versionedBy = versionedBy;
  }

  public boolean isActiveVersion() {
    return isActiveVersion;
  }

  public void setActiveVersion(boolean activeVersion) {
    isActiveVersion = activeVersion;
  }
}
