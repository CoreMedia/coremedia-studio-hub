package com.coremedia.blueprint.studio.connectors.rest.representation;

public class ConnectorItemRepresentation extends ConnectorEntityRepresentation {
  private boolean downloadable;
  private long size;
  private String targetContentType;
  private String itemType;

  private String openInTabUrl;
  private String downloadUrl;
  private String streamUrl;

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getItemType() {
    return itemType;
  }

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public boolean isDownloadable() {
    return downloadable;
  }

  public void setDownloadable(boolean downloadable) {
    this.downloadable = downloadable;
  }

  public String getOpenInTabUrl() {
    return openInTabUrl;
  }

  public void setOpenInTabUrl(String openInTabUrl) {
    this.openInTabUrl = openInTabUrl;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public String getStreamUrl() {
    return streamUrl;
  }

  public void setStreamUrl(String streamUrl) {
    this.streamUrl = streamUrl;
  }

  public String getTargetContentType() {
    return targetContentType;
  }

  public void setTargetContentType(String targetContentType) {
    this.targetContentType = targetContentType;
  }
}
