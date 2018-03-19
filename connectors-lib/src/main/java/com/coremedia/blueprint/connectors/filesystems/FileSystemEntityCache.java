package com.coremedia.blueprint.connectors.filesystems;

import com.coremedia.blueprint.connectors.api.ConnectorId;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple cache implementation for file system based connectors.
 * This ensure that remote folders or file information are not read multiple times.
 */
public class FileSystemEntityCache<T> {
  private Map<String,CacheItem<T>> itemCache = new HashMap<>();

  public boolean contains(ConnectorId categoryId) {
    return itemCache.containsKey(categoryId.toString());
  }

  public CacheItem<T> get(ConnectorId categoryId) {
    String key = categoryId.toString();
    if(itemCache.containsKey(key)) {
      return itemCache.get(key);
    }
    return null;
  }

  public void cache(ConnectorId categoryId, CacheItem<T> cacheItem) {
    itemCache.put(categoryId.toString(), cacheItem);
  }

  public void invalidate() {
    itemCache.clear();
  }

  public void invalidate(ConnectorId categoryId) {
    String key = categoryId.toString();
    if(itemCache.containsKey(key)) {
      itemCache.remove(key);
    }
  }
}
