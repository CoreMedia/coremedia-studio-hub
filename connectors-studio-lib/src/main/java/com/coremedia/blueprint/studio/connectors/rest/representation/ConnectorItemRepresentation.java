package com.coremedia.blueprint.studio.connectors.rest.representation;

import java.net.URI;

public class ConnectorItemRepresentation extends ConnectorEntityRepresentation {
  private URI previewUri;

  private boolean downloadable;
  private long size;
  private String contentType;
  private String itemType;
  private String status;

  private String openInTabUrl;
  private String downloadUrl;
  private String streamUrl;

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getItemType() {
    return itemType;
  }

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public URI getPreviewUri() {
    return previewUri;
  }

  public void setPreviewUri(URI previewUri) {
    this.previewUri = previewUri;
  }

  public boolean isDownloadable() {
    return downloadable;
  }

  public void setDownloadable(boolean downloadable) {
    this.downloadable = downloadable;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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
}
