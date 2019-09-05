package com.coremedia.blueprint.connectors.shutterstock.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * *                 "preview": {
 *  *                     "height": 69,
 *  *                     "url": "https://image.shutterstock.com/display_pic_with_logo/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *  *                     "width": 450
 *  *                 },
 *  *                 "small_thumb": {
 *  *                     "height": 15,
 *  *                     "url": "https://thumb7.shutterstock.com/thumb_small/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *  *                     "width": 100
 *  *                 },
 *  *                 "large_thumb": {
 *  *                     "height": 23,
 *  *                     "url": "https://thumb7.shutterstock.com/thumb_large/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *  *                     "width": 150
 *  *                 },
 *  *                 "huge_thumb": {
 *  *                     "height": 260,
 *  *                     "url": "https://image.shutterstock.com/image-photo/collage-ten-charming-glad-carefree-260nw-1424389784.jpg",
 *  *                     "width": 1696
 *  *                 },
 *  *                 "preview_1000": {
 *  *                     "url": "https://ak.picdn.net/shutterstock/photos/1424389784/watermark_1000/0615cd0e995b2474b96e71f8ba2faba3/preview_1000-1424389784.jpg",
 *  *                     "width": 1000,
 *  *                     "height": 154
 *  *                 },
 *  *                 "preview_1500": {
 *  *                     "url": "https://image.shutterstock.com/z/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *  *                     "width": 1500,
 *  *                     "height": 232
 *  *                 }
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Assets {
  private Asset preview;

  @JsonProperty("small_thumb")
  private Asset smallThumb;

  @JsonProperty("large_thumb")
  private Asset largeThumb;

  @JsonProperty("huge_thumb")
  private Asset hugeThumb;

  @JsonProperty("preview_1000")
  private Asset preview1000;

  @JsonProperty("preview_1500")
  private Asset preview1500;

  public Asset getPreview() {
    return preview;
  }

  public void setPreview(Asset preview) {
    this.preview = preview;
  }

  public Asset getSmallThumb() {
    return smallThumb;
  }

  public void setSmallThumb(Asset smallThumb) {
    this.smallThumb = smallThumb;
  }

  public Asset getLargeThumb() {
    return largeThumb;
  }

  public void setLargeThumb(Asset largeThumb) {
    this.largeThumb = largeThumb;
  }

  public Asset getHugeThumb() {
    return hugeThumb;
  }

  public void setHugeThumb(Asset hugeThumb) {
    this.hugeThumb = hugeThumb;
  }

  public Asset getPreview1000() {
    return preview1000;
  }

  public void setPreview1000(Asset preview1000) {
    this.preview1000 = preview1000;
  }

  public Asset getPreview1500() {
    return preview1500;
  }

  public void setPreview1500(Asset preview1500) {
    this.preview1500 = preview1500;
  }
}
