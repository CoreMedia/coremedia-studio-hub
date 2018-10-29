package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.CHANNEL_ID;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.CREDENTIALS_JSON;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.USER;

/**
 *
 */
public class YouTubeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeService.class);

  private YouTubeConnector youTubeConnector;

  public void setConnector(YouTubeConnector connector) {
    this.youTubeConnector = connector;
  }

  //---------------- Service Methods -----------------------------------------------------------------------------------

  @Cacheable(value = "youTubePlayListByUserCache", key = "'playlistByUser_' + #connectionId + '_' + #user", cacheManager = "cacheManagerYouTube")
  public List<Playlist> getPlaylistsByUser(String connectionId, String user) {
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

  @Cacheable(value = "youTubePlayListByChannelCache", key = "'playlistByChannel_' + #connectionId + '_' + #channelId", cacheManager = "cacheManagerYouTube")
  public List<Playlist> getPlaylistsByChannel(String connectionId, String channelId) {
    List<Playlist> playlists = new ArrayList<>();
    try {
      PlaylistListResponse playlistListResponse = youTubeConnector.getPlayListResponse(channelId);
      playlists.addAll(playlistListResponse.getItems());
    } catch (IOException e) {
      LOGGER.error("Failed to read playlists by channel  '" + channelId + ": " + e.getMessage(), e);
    }
    return playlists;
  }


  @Cacheable(value = "youTubeVideoCache", key = "'video_' + #connectionId + '_' + #videoId", cacheManager = "cacheManagerYouTube")
  public Video getVideo(String connectionId, String videoId) {
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

  @Cacheable(value = "youTubeVideosCache", key = "'videos_' + #connectionId + '_' + #channelId", cacheManager = "cacheManagerYouTube")
  public List<Video> getVideos(String connectionId, String channelId) {
    List<Video> result = new ArrayList<>();
    try {
      SearchListResponse response = youTubeConnector.getSearchListResponse(channelId, " ");
      List<SearchResult> searchResults = response.getItems();

      if (searchResults != null) {
        for (SearchResult searchResult : searchResults) {
          String videoId = searchResult.getId().getVideoId();
          Video video = getVideo(connectionId, videoId);
          result.add(video);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Failed to get videos for channel '" + channelId + "': " + e.getMessage(), e);
    }
    return result;
  }

  @Cacheable(value = "youTubePlaylistItemsCache", key = "'playlistItems_' + #connectionId + '_' + #playlistId", cacheManager = "cacheManagerYouTube")
  public List<PlaylistItem> getPlaylistItems(String connectionId, String playlistId) {
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
}
