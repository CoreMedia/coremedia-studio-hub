package com.coremedia.blueprint.connectors.celum.rest;

public class PreviewInformation {

  private int thumbWidth;
  private int thumbHeight;
  private String thumbUrl;
  private String previewUrl;
  private String largePreviewUrl;
  private String alternativeFileName;

  public int getThumbWidth() {
    return thumbWidth;
  }

  public void setThumbWidth(int thumbWidth) {
    this.thumbWidth = thumbWidth;
  }

  public int getThumbHeight() {
    return thumbHeight;
  }

  public void setThumbHeight(int thumbHeight) {
    this.thumbHeight = thumbHeight;
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setThumbUrl(String thumbUrl) {
    this.thumbUrl = thumbUrl;
  }

  public String getPreviewUrl() {
    return previewUrl;
  }

  public void setPreviewUrl(String previewUrl) {
    this.previewUrl = previewUrl;
  }

  public String getLargePreviewUrl() {
    return largePreviewUrl;
  }

  public void setLargePreviewUrl(String largePreviewUrl) {
    this.largePreviewUrl = largePreviewUrl;
  }

  public String getAlternativeFileName() {
    return alternativeFileName;
  }

  public void setAlternativeFileName(String alternativeFileName) {
    this.alternativeFileName = alternativeFileName;
  }
}
