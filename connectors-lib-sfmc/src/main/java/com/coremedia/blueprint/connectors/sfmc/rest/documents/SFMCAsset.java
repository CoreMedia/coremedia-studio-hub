package com.coremedia.blueprint.connectors.sfmc.rest.documents;

import java.util.Date;

/**
 *
 */
public class SFMCAsset extends SFMCEntity {
  private String customerKey;
  private String contentType;
  private SFMCAssetType assetType;
  private Date createdDate;
  private Date modifiedDate;
  private SFMCCategory category;
  private SFMCUser owner;
  private SFMCUser modifiedBy;
  private SFMCUser createdBy;
  private SFMCThumbnailUrl thumbnail;
  private SFMCEntity status;
  private SFMCFileProperties fileProperties;
  private SFMCViews views;

  public String getCustomerKey() {
    return customerKey;
  }

  public void setCustomerKey(String customerKey) {
    this.customerKey = customerKey;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public SFMCAssetType getAssetType() {
    return assetType;
  }

  public void setAssetType(SFMCAssetType assetType) {
    this.assetType = assetType;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public SFMCCategory getCategory() {
    return category;
  }

  public void setCategory(SFMCCategory category) {
    this.category = category;
  }

  public SFMCUser getOwner() {
    return owner;
  }

  public void setOwner(SFMCUser owner) {
    this.owner = owner;
  }

  public SFMCUser getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(SFMCUser modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public SFMCUser getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(SFMCUser createdBy) {
    this.createdBy = createdBy;
  }

  public SFMCThumbnailUrl getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(SFMCThumbnailUrl thumbnail) {
    this.thumbnail = thumbnail;
  }

  public SFMCEntity getStatus() {
    return status;
  }

  public void setStatus(SFMCEntity status) {
    this.status = status;
  }

  public SFMCFileProperties getFileProperties() {
    return fileProperties;
  }

  public void setFileProperties(SFMCFileProperties fileProperties) {
    this.fileProperties = fileProperties;
  }

  public SFMCViews getViews() {
    return views;
  }

  public void setViews(SFMCViews views) {
    this.views = views;
  }
}
