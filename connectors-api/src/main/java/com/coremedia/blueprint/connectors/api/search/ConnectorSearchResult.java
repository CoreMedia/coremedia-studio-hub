package com.coremedia.blueprint.connectors.api.search;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The search result entity of a connector service
 * @param <T> the entity type of a service
 */
public class ConnectorSearchResult<T> {
  private List<T> searchResult = new ArrayList<>();

  public ConnectorSearchResult(@NonNull List<T> searchResult) {
    this.searchResult = searchResult;
  }

  public void merge(ConnectorSearchResult<T> result) {
    this.searchResult.addAll(result.getSearchResult());
  }

  public List<T> getSearchResult() {
    return this.searchResult;
  }

  public int getTotalCount() {
    return searchResult.size();
  }

  public String toString() {
    return "SearchResult{searchResult=" + this.searchResult + ", totalCount=" + searchResult.size() + "\"}'";
  }

}
