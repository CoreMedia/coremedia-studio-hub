package com.coremedia.blueprint.connectors.sfmc.rest.documents;

import java.util.List;

/**
 *  "count": 3,
 *     "page": 1,
 *     "pageSize": 50,
 *     "links": {},
 *     "items": [
 */
abstract public class SFMCCollection<T> {
  private int count;
  private int page;
  private int pageSize;
  private List<T> items;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }
}
