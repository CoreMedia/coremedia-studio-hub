package com.coremedia.blueprint.connectors.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;

/**
 *
 */
public class YouTubeConnector {

  private static final String YOUTUBE_VIDEO_TYPE_SNIPPET = "video";
  private static final String REQUEST_TYPE_SNIPPET = "snippet";
  private static final String REQUEST_TYPE_STATISTICS = "statistics";
  private static final String REQUEST_TYPE_BY_USER = "snippet,contentDetails,statistics";

  private YouTube youTube;

  YouTubeConnector(YouTube youTube) {
    this.youTube = youTube;
  }

  PlaylistItemListResponse getPlayListItemResponse(String playlistId) throws IOException {
    return youTube.playlistItems().list(REQUEST_TYPE_SNIPPET).setPlaylistId(playlistId).execute();
  }

  PlaylistListResponse getPlayListResponse(String channelId) throws IOException {
    return youTube.playlists().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).execute();
  }

  VideoListResponse getVideoListResponse(String videoId) throws IOException {
    return youTube.videos().list(REQUEST_TYPE_SNIPPET + "," + REQUEST_TYPE_STATISTICS).setId(videoId).execute();
  }

  SearchListResponse getSearchListResponse(String channelId, String term) throws IOException {
    return youTube.search().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).setType(YOUTUBE_VIDEO_TYPE_SNIPPET).setQ(term).execute();
  }

  ChannelListResponse getChannelList(String user) throws IOException {
    YouTube.Channels.List channelsListByUsernameRequest = youTube.channels().list(REQUEST_TYPE_BY_USER);
    channelsListByUsernameRequest.setForUsername(user);

    return channelsListByUsernameRequest.execute();
  }
}
