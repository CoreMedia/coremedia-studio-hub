package com.coremedia.blueprint.connectors.youtube.caching;

import com.coremedia.blueprint.connectors.youtube.YouTubeConnector;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class VideoCacheKey extends CacheKey<Video> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VideoCacheKey.class);

  private YouTubeConnector youTubeConnector;
  private String videoId;

  public VideoCacheKey(YouTubeConnector youTubeConnector, String videoId) {
    this.youTubeConnector = youTubeConnector;
    this.videoId = videoId;
  }

  @NonNull
  public String getDependencyKey() {
    return VideoCacheKey.class.getName() + ":" + videoId;
  }

  @Override
  public Video evaluate(Cache cache) throws Exception {
    Cache.dependencyOn(getDependencyKey());
    try {
      VideoListResponse videoListResponse = youTubeConnector.getVideoListResponse(videoId);
      List<Video> videos = videoListResponse.getItems();
      if (videos != null && videos.size() > 0) {
        return videos.get(0);
      }
    } catch (IOException e) {
      LOGGER.error("Failed to get vidoe '" + videoId + "': " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VideoCacheKey)) {
      return false;
    }

    VideoCacheKey that = (VideoCacheKey) o;
    return videoId.equals(that.videoId);
  }

  @Override
  public int hashCode() {
    return getDependencyKey().hashCode();
  }
}
