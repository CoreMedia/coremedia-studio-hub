package com.coremedia.blueprint.connectors.celum.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class Binary {
  @JsonProperty("@odata.mediaReadLink")
  private String mediaLink;

  private int asset;
  private String type;
  private int format;

  public String getMediaLink() {
    return mediaLink;
  }

  public void setMediaLink(String mediaLink) {
    this.mediaLink = mediaLink;
  }

  public int getAsset() {
    return asset;
  }

  public void setAsset(int asset) {
    this.asset = asset;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getFormat() {
    return format;
  }

  public void setFormat(int format) {
    this.format = format;
  }
}
