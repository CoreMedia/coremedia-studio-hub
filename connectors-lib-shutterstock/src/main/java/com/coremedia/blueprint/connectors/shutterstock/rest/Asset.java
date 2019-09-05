package com.coremedia.blueprint.connectors.shutterstock.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * *                 "preview": {
 *  *                     "height": 69,
 *  *                     "url": "https://image.shutterstock.com/display_pic_with_logo/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *  *                     "width": 450
 *  *                 },
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Asset {
  private int height;
  private String url;
  private int width;

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }
}
