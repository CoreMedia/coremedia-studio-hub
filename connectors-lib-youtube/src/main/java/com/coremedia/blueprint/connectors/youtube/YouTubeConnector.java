package com.coremedia.blueprint.connectors.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 */
public class YouTubeConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeConnector.class);

  private static final String YOUTUBE_VIDEO_TYPE_SNIPPET = "video";
  private static final String REQUEST_TYPE_SNIPPET = "snippet";
  private static final String REQUEST_TYPE_STATISTICS = "statistics";
  private static final String REQUEST_TYPE_BY_USER = "snippet,contentDetails,statistics";

  private YouTube youTube;

  YouTubeConnector(YouTube youTube) {
    this.youTube = youTube;
  }

  public PlaylistItemListResponse getPlayListItemResponse(String playlistId) throws IOException {
    LOGGER.info("YouTube: requesting playlist " + playlistId);
    return youTube.playlistItems().list(REQUEST_TYPE_SNIPPET).setPlaylistId(playlistId).execute();
  }

  public PlaylistListResponse getPlayListResponse(String channelId) throws IOException {
    LOGGER.info("YouTube: requesting playlist of channel " + channelId);
    return youTube.playlists().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).execute();
  }

  public VideoListResponse getVideoListResponse(String videoId) throws IOException {
    LOGGER.info("YouTube: requesting video " + videoId);
    return youTube.videos().list(REQUEST_TYPE_SNIPPET + "," + REQUEST_TYPE_STATISTICS).setId(videoId).execute();
  }

  public SearchListResponse getSearchListResponse(String channelId, String term) throws IOException {
    LOGGER.info("YouTube: requesting search list for channel " + channelId);
    return youTube.search().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).setType(YOUTUBE_VIDEO_TYPE_SNIPPET).setQ(term).execute();
  }

  public ChannelListResponse getChannelList(String user) throws IOException {
    LOGGER.info("YouTube: requesting channels for user " + user);
    YouTube.Channels.List channelsListByUsernameRequest = youTube.channels().list(REQUEST_TYPE_BY_USER);
    channelsListByUsernameRequest.setForUsername(user);

    return channelsListByUsernameRequest.execute();
  }
}
