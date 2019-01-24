package com.coremedia.blueprint.connectors.sfmc.rest.documents;

/**
 *
 */
public class SFMCFileProperties {
  private String extension;
  private int fileSize;
  private int width;
  private int height;
  private String publishedURL;

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public int getFileSize() {
    return fileSize;
  }

  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getPublishedURL() {
    return publishedURL;
  }

  public void setPublishedURL(String publishedURL) {
    this.publishedURL = publishedURL;
  }
}
