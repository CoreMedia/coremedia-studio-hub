package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.google.api.services.youtube.model.Playlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class YouTubeCache {
  private Map<String,YouTubeCacheItem> cache = new HashMap<>();

  private List<Playlist> playlists = new ArrayList<>();

  public YouTubeCacheItem get(ConnectorId id) {
    return cache.get(id.toString());
  }

  void invalidate() {
    this.playlists.clear();
    this.cache.clear();
  }

  List<Playlist> getPlaylists() {
    return playlists;
  }

  void setPlaylists(List<Playlist> playlists) {
    this.playlists = playlists;
  }

  public void cache(YouTubeCacheItem item) {
    cache.put(item.getId().toString(), item);
  }
}
