package com.coremedia.blueprint.connectors.youtube;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class YouTubeConnectorVideo extends YouTubeConnectorEntity implements ConnectorItem {

  private final YouTubeConnectorServiceImpl service;
  private Video video;

  YouTubeConnectorVideo(YouTubeConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, Video video) {
    super(parent, context, connectorId);
    this.service = service;
    this.video = video;
    setName(video.getSnippet().getTitle());
  }

  @Override
  public Date getLastModified() {
    VideoSnippet snippet = video.getSnippet();
    return new Date(snippet.getPublishedAt().getValue() + snippet.getPublishedAt().getTimeZoneShift() * 60000L);
  }

  @Nullable
  @Override
  public String getThumbnailUrl() {
    ThumbnailDetails thumbnails = video.getSnippet().getThumbnails();
    if(thumbnails != null && thumbnails.getMedium() != null) {
      return thumbnails.getMedium().getUrl();
    }

    return null;
  }

  @NonNull
  @Override
  public String getItemType() {
    return "youtube";
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getOpenInTabUrl() {
    return "https://www.youtube.com/watch?v=" + video.getId();
  }

  @Nullable
  @Override
  public String getDescription() {
    return video.getSnippet().getDescription();
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    String html = ConnectorItem.super.getPreviewHtml();
    if(html != null) {
      return MessageFormat.format(html, video.getId());
    }
    return null;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();

    VideoSnippet snippet = video.getSnippet();
    data.put("channel", snippet.getChannelTitle());
    data.put("description", snippet.getDescription());
    data.put("language", snippet.getDefaultAudioLanguage());

    ThumbnailDetails thumbnails = video.getSnippet().getThumbnails();
    if(thumbnails != null) {
      Thumbnail standard = thumbnails.getStandard();
      if(standard != null) {
        data.put("thumbnail", standard.getUrl());
      }
    }

    VideoStatistics statistics = video.getStatistics();
    if(statistics != null) {
      data.put("comments", statistics.getCommentCount());
      data.put("likes", statistics.getLikeCount());
      data.put("dislikes", statistics.getDislikeCount());
      data.put("views", statistics.getViewCount());
      data.put("comments", statistics.getCommentCount());
    }

    return () -> data;
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return null;
  }

  public Video getVideo() {
    return video;
  }
}
