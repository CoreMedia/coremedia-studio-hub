package com.coremedia.blueprint.connectors.youtube.caching;

import com.coremedia.blueprint.connectors.youtube.YouTubeConnector;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
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
public class PlaylistByUserCacheKey extends CacheKey<List<Playlist>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistByUserCacheKey.class);

  private YouTubeConnector youTubeConnector;
  private String connectionId;
  private String user;

  public PlaylistByUserCacheKey(YouTubeConnector youTubeConnector, String connectionId, String user) {
    this.youTubeConnector = youTubeConnector;
    this.connectionId = connectionId;
    this.user = user;
  }

  @NonNull
  public String getDependencyKey() {
    return PlaylistByUserCacheKey.class.getName() + ":" + connectionId + ":" + user;
  }

  @Override
  public List<Playlist> evaluate(Cache cache) throws Exception {
    Cache.dependencyOn(getDependencyKey());
    List<Playlist> playlists = new ArrayList<>();
    try {
      ChannelListResponse r = youTubeConnector.getChannelList(user);
      List<Channel> items = r.getItems();
      for (Channel item : items) {
        PlaylistListResponse channelPlayListResponse = youTubeConnector.getPlayListResponse(item.getId());
        List<Playlist> userPlaylists = channelPlayListResponse.getItems();
        playlists.addAll(userPlaylists);
      }
    } catch (IOException e) {
      LOGGER.error("Failed to read playlists by user '" + user + ": " + e.getMessage(), e);
    }
    return playlists;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PlaylistByUserCacheKey)) {
      return false;
    }

    PlaylistByUserCacheKey that = (PlaylistByUserCacheKey) o;
    return (user + connectionId).equals((that.user + that.connectionId));
  }

  @Override
  public int hashCode() {
    return getDependencyKey().hashCode();
  }
}
