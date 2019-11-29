package com.coremedia.blueprint.connectors.celum.rest;

public class FileInformation {

  private int originalFileSize;

  private String originalFileName;

  private String fileExtension;

  public int getOriginalFileSize() {
    return originalFileSize;
  }

  public void setOriginalFileSize(int originalFileSize) {
    this.originalFileSize = originalFileSize;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }
}
