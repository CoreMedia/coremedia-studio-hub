package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.youtube.caching.PlaylistByChannelCacheKey;
import com.coremedia.blueprint.connectors.youtube.caching.PlaylistByUserCacheKey;
import com.coremedia.blueprint.connectors.youtube.caching.PlaylistitemsCacheKey;
import com.coremedia.blueprint.connectors.youtube.caching.VideoCacheKey;
import com.coremedia.blueprint.connectors.youtube.caching.VideosByChannelCacheKey;
import com.coremedia.cache.Cache;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class YouTubeService {
  private YouTubeConnector youTubeConnector;
  private Cache cache;

  public void setConnector(YouTubeConnector connector) {
    this.youTubeConnector = connector;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }
  //---------------- Service Methods -----------------------------------------------------------------------------------

  public List<Playlist> getPlaylistsByUser(String connectionId, String user) {
    PlaylistByUserCacheKey cacheKey = new PlaylistByUserCacheKey(youTubeConnector, connectionId, user);
    return cache.get(cacheKey);
  }

  public List<Playlist> getPlaylistsByChannel(String connectionId, String channelId) {
    PlaylistByChannelCacheKey cacheKey = new PlaylistByChannelCacheKey(youTubeConnector, channelId);
    return cache.get(cacheKey);
  }

  public Video getVideo(String connectionId, String videoId) {
    VideoCacheKey cacheKey = new VideoCacheKey(youTubeConnector, videoId);
    return cache.get(cacheKey);
  }

  public List<Video> getVideos(String connectionId, String channelId) {
    VideosByChannelCacheKey cacheKey = new VideosByChannelCacheKey(youTubeConnector, channelId);
    List<SearchResult> searchResults = cache.get(cacheKey);

    List<Video> result = new ArrayList<>();
    for (SearchResult searchResult : searchResults) {
      String videoId = searchResult.getId().getVideoId();
      Video video = getVideo(connectionId, videoId);
      result.add(video);
    }

    return result;
  }

  public List<PlaylistItem> getPlaylistItems(String connectionId, String playlistId) {
    PlaylistitemsCacheKey cacheKey = new PlaylistitemsCacheKey(youTubeConnector, playlistId);
    return cache.get(cacheKey);
  }
}
