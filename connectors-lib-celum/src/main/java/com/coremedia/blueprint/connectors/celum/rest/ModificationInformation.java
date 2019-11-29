package com.coremedia.blueprint.connectors.celum.rest;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ModificationInformation {

  private int createdBy;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date creationDate;
  private boolean modified;
  private int lastModifiedBy;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date lastModificationDate;

  public int getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(int createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified(boolean modified) {
    this.modified = modified;
  }

  public int getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(int lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public Date getLastModificationDate() {
    return lastModificationDate;
  }

  public void setLastModificationDate(Date lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }
}
