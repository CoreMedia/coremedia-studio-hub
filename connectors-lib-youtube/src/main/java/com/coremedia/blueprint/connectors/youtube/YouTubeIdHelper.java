package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorId;

/**
 * Helper to differ between Playlists and Videos.
 */
public class YouTubeIdHelper {
  private static final String PLAYLIST_ID = "playlist#";//playlist#[playlistId]
  private static final String VIDEO_ID = "video#"; //video#[playlistId]#[videoId]

  static String createPlaylistId(String id) {
    return PLAYLIST_ID + id;
  }

  static String createVideoId(String playlistId, String videoId) {
    return VIDEO_ID + playlistId + "#" + videoId;
  }

  static String getExternalId(ConnectorId id) {
    String[] split = id.getExternalId().split("#");
    return split[split.length - 1];
  }

  static String getPlaylistId(ConnectorId id) {
    if (id.isRootId()) {
      return "";
    }
    String[] split = id.getExternalId().split("#");
    return split[1];
  }
}
