package com.coremedia.blueprint.connectors.shutterstock.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 *{
 *     "page": 1,
 *     "per_page": 20,
 *     "total_count": 16433315,
 *     "search_id": "13lBp8R8V5-UyrXF9_Gcxg",
 *     "data": [
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SearchResult {
  private int page;

  @JsonProperty("per_page")
  private int perPage;

  @JsonProperty("total_count")
  private int totalCount;

  @JsonProperty("search_id")
  private String searchId;
  private List<Picture> data;

  public List<Picture> getData() {
    return data;
  }

  public void setData(List<Picture> data) {
    this.data = data;
  }

  public String getSearchId() {
    return searchId;
  }

  public void setSearchId(String searchId) {
    this.searchId = searchId;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public int getPerPage() {
    return perPage;
  }

  public void setPerPage(int perPage) {
    this.perPage = perPage;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }
}
