package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class YouTubeCacheItem {
  private Playlist playlist;
  private Video video;
  private ConnectorId id;
  private List<PlaylistItem> playlistItems = new ArrayList<>();
  private List<Video> videos = new ArrayList<>();

  YouTubeCacheItem(ConnectorId id, List<Video> videos) {
    this(id, null, null, videos, null);
  }

  YouTubeCacheItem(ConnectorId id, Playlist playlist, List<PlaylistItem> playlistItems) {
    this(id, playlist, playlistItems, null, null);
  }

  YouTubeCacheItem(ConnectorId id, Playlist playlist, List<PlaylistItem> playlistItems, List<Video> videos, Video video) {
    this.id = id;
    this.playlist = playlist;
    this.playlistItems = playlistItems;
    this.video = video;
    this.videos = videos;
  }

  Playlist getPlaylist() {
    return playlist;
  }

  public Video getVideo() {
    return video;
  }

  public ConnectorId getId() {
    return id;
  }

  List<PlaylistItem> getPlaylistItems() {
    return playlistItems;
  }

  public List<Video> getVideos() {
    return videos;
  }
}
