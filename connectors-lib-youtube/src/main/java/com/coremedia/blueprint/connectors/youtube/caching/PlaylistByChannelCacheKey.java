package com.coremedia.blueprint.connectors.youtube.caching;

import com.coremedia.blueprint.connectors.youtube.YouTubeConnector;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PlaylistByChannelCacheKey extends CacheKey<List<Playlist>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistByChannelCacheKey.class);

  private YouTubeConnector youTubeConnector;
  private String channelId;

  public PlaylistByChannelCacheKey(YouTubeConnector youTubeConnector, String channelId) {
    this.youTubeConnector = youTubeConnector;
    this.channelId = channelId;
  }

  @NonNull
  public String getDependencyKey() {
    return PlaylistByChannelCacheKey.class.getName() + ":" + channelId;
  }

  @Override
  public List<Playlist> evaluate(Cache cache) throws Exception {
    Cache.dependencyOn(getDependencyKey());
    List<Playlist> playlists = new ArrayList<>();
    try {
      PlaylistListResponse playlistListResponse = youTubeConnector.getPlayListResponse(channelId);
      playlists.addAll(playlistListResponse.getItems());
    } catch (IOException e) {
      LOGGER.error("Failed to read playlists by channel  '" + channelId + ": " + e.getMessage(), e);
    }
    return playlists;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PlaylistByChannelCacheKey)) {
      return false;
    }

    PlaylistByChannelCacheKey that = (PlaylistByChannelCacheKey) o;
    return channelId.equals(that.channelId);
  }

  @Override
  public int hashCode() {
    return getDependencyKey().hashCode();
  }
}
