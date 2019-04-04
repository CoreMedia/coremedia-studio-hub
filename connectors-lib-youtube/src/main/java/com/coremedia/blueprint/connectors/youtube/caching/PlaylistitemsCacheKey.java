package com.coremedia.blueprint.connectors.youtube.caching;

import com.coremedia.blueprint.connectors.youtube.YouTubeConnector;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class PlaylistitemsCacheKey extends CacheKey<List<PlaylistItem>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistitemsCacheKey.class);

  private YouTubeConnector youTubeConnector;
  private String playlistId;

  public PlaylistitemsCacheKey(YouTubeConnector youTubeConnector, String playlistId) {
    this.youTubeConnector = youTubeConnector;
    this.playlistId = playlistId;
  }

  @NonNull
  public String getDependencyKey() {
    return PlaylistitemsCacheKey.class.getName() + ":" + playlistId;
  }

  @Override
  public List<PlaylistItem> evaluate(Cache cache) throws Exception {
    Cache.dependencyOn(getDependencyKey());
    try {
      PlaylistItemListResponse playlistItemsResponse = youTubeConnector.getPlayListItemResponse(playlistId);
      List<PlaylistItem> items = playlistItemsResponse.getItems();
      if (items != null) {
        return items;
      }
    } catch (IOException e) {
      LOGGER.error("Failed to read playlist items: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PlaylistitemsCacheKey)) {
      return false;
    }

    PlaylistitemsCacheKey that = (PlaylistitemsCacheKey) o;
    return playlistId.equals(that.playlistId);
  }

  @Override
  public int hashCode() {
    return getDependencyKey().hashCode();
  }
}
