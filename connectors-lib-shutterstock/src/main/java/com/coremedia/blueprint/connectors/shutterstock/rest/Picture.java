package com.coremedia.blueprint.connectors.shutterstock.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * {
 *             "id": "1424389784",
 *             "aspect": 6.4758,
 *             "assets": {
 *                 "preview": {
 *                     "height": 69,
 *                     "url": "https://image.shutterstock.com/display_pic_with_logo/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *                     "width": 450
 *                 },
 *                 "small_thumb": {
 *                     "height": 15,
 *                     "url": "https://thumb7.shutterstock.com/thumb_small/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *                     "width": 100
 *                 },
 *                 "large_thumb": {
 *                     "height": 23,
 *                     "url": "https://thumb7.shutterstock.com/thumb_large/2797510/1424389784/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *                     "width": 150
 *                 },
 *                 "huge_thumb": {
 *                     "height": 260,
 *                     "url": "https://image.shutterstock.com/image-photo/collage-ten-charming-glad-carefree-260nw-1424389784.jpg",
 *                     "width": 1696
 *                 },
 *                 "preview_1000": {
 *                     "url": "https://ak.picdn.net/shutterstock/photos/1424389784/watermark_1000/0615cd0e995b2474b96e71f8ba2faba3/preview_1000-1424389784.jpg",
 *                     "width": 1000,
 *                     "height": 154
 *                 },
 *                 "preview_1500": {
 *                     "url": "https://image.shutterstock.com/z/stock-photo-collage-of-ten-charming-glad-carefree-nice-attractive-shiny-modern-delightful-girls-millennials-1424389784.jpg",
 *                     "width": 1500,
 *                     "height": 232
 *                 }
 *             },
 *             "contributor": {
 *                 "id": "2797510"
 *             },
 *             "description": "Collage of ten charming glad carefree nice attractive shiny modern delightful girls millennials person youngsters having good mood flying air isolated over colorful background travel summer concept",
 *             "image_type": "photo",
 *             "has_model_release": false,
 *             "media_type": "image"
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Picture {
  private String id;
  private String description;

  @JsonProperty("image_type")
  private String imageType;

  @JsonProperty("has_model_release")
  private String hasModelRelease;

  @JsonProperty("media_type")
  private String mediaType;

  private String aspect;

  private Assets assets;

  private List<Category> categories;

  private List<String> keywords;

  private Contributor contributor;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageType() {
    return imageType;
  }

  public void setImageType(String imageType) {
    this.imageType = imageType;
  }

  public String getHasModelRelease() {
    return hasModelRelease;
  }

  public void setHasModelRelease(String hasModelRelease) {
    this.hasModelRelease = hasModelRelease;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getAspect() {
    return aspect;
  }

  public void setAspect(String aspect) {
    this.aspect = aspect;
  }

  public Assets getAssets() {
    return assets;
  }

  public void setAssets(Assets assets) {
    this.assets = assets;
  }

  public Contributor getContributor() {
    return contributor;
  }

  public void setContributor(Contributor contributor) {
    this.contributor = contributor;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }
}
