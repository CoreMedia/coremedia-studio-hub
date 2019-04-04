package com.coremedia.blueprint.connectors.youtube.caching;

import com.coremedia.blueprint.connectors.youtube.YouTubeConnector;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class VideosByChannelCacheKey extends CacheKey<List<SearchResult>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VideosByChannelCacheKey.class);

  private YouTubeConnector youTubeConnector;
  private String channelId;

  public VideosByChannelCacheKey(YouTubeConnector youTubeConnector, String channelId) {
    this.youTubeConnector = youTubeConnector;
    this.channelId = channelId;
  }

  @NonNull
  public String getDependencyKey() {
    return VideosByChannelCacheKey.class.getName() + ":" + channelId;
  }

  @Override
  public List<SearchResult> evaluate(Cache cache) throws Exception {
    Cache.dependencyOn(getDependencyKey());
    try {
      SearchListResponse response = youTubeConnector.getSearchListResponse(channelId, " ");
      List<SearchResult> searchResults = response.getItems();

      if (searchResults != null) {
        return searchResults;
      }
    } catch (IOException e) {
      LOGGER.error("Failed to get videos for channel '" + channelId + "': " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VideosByChannelCacheKey)) {
      return false;
    }

    VideosByChannelCacheKey that = (VideosByChannelCacheKey) o;
    return channelId.equals(that.channelId);
  }

  @Override
  public int hashCode() {
    return getDependencyKey().hashCode();
  }
}
