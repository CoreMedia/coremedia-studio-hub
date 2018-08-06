package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistSnippet;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YouTubeConnectorCategory extends YouTubeConnectorEntity implements ConnectorCategory {
  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();
  private final YouTubeConnectorServiceImpl service;
  private Playlist playlist;

  YouTubeConnectorCategory(YouTubeConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, Playlist youtubePlaylist, String name) {
    super(parent, context, connectorId);
    this.service = service;
    this.playlist = youtubePlaylist;
    setName(name);
  }

  @Override
  public String getType() {
    if(getConnectorId().isRootId()) {
      return "youtubechannel";
    }
    return "playlist";
  }

  @Override
  public Date getLastModified() {
    if(playlist != null) {
      PlaylistSnippet snippet = playlist.getSnippet();
      return new Date(snippet.getPublishedAt().getValue() + snippet.getPublishedAt().getTimeZoneShift() * 60000L);
    }
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    return items;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  public void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  public void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  public Playlist getPlaylist() {
    return playlist;
  }

  @Override
  public boolean refresh(@NonNull ConnectorContext context) {
    return service.refresh(context, this);
  }
}
