package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.CHANNEL_ID;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.CREDENTIALS_JSON;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.DISPLAY_NAME;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.USER;

public class YouTubeConnectorServiceImpl implements ConnectorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeConnectorServiceImpl.class);

  private static final String HTTPS_WWW_GOOGLEAPIS_COM_AUTH_YOUTUBE_FORCE_SSL = "https://www.googleapis.com/auth/youtube.force-ssl";

  private static final String YOUTUBE_VIDEO_TYPE_SNIPPET = "video";
  private static final String REQUEST_TYPE_SNIPPET = "snippet";
  private static final String REQUEST_TYPE_STATISTICS = "statistics";
  private static final String REQUEST_TYPE_BY_USER = "snippet,contentDetails,statistics";

  private ConnectorContext context;
  private YouTube youTube;

  private YouTubeConnectorCategory rootCategory;
  private YouTubeCache cache;

  @Override
  public boolean init(@Nonnull ConnectorContext context) throws ConnectorException {
    try {
      this.context = context;
      youTube = getYouTube();
      cache = new YouTubeCache();
      rebuildCache();
      return true;
    } catch (ConnectorException e) {
      return false;
    }
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorId id) throws ConnectorException {
    YouTubeCacheItem cacheItem = cache.get(id);
    Video video = cacheItem.getVideo();
    Playlist playlist = cacheItem.getPlaylist();
    ConnectorId categoryId = ConnectorId.createRootId(context.getConnectionId());

    //again, we have to check here if the video belongs to a playlist or not
    if (playlist != null) {
      categoryId = ConnectorId.createCategoryId(context.getConnectionId(), playlist.getId());
    }
    YouTubeConnectorCategory category = new YouTubeConnectorCategory(null, context, categoryId, playlist, null);
    return new YouTubeConnectorVideo(this, category, context, id, video);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorId id) throws ConnectorException {
    if (id.isRootId()) {
      return rootCategory;
    }

    YouTubeCacheItem cacheItem = cache.get(id);
    Playlist playlist = cacheItem.getPlaylist();
    String name = playlist.getSnippet().getTitle();
    YouTubeConnectorCategory category = new YouTubeConnectorCategory(getRootCategory(), context, id, playlist, name);
    category.setItems(getItems(category));
    return category;
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory() {
    if (rootCategory == null) {
      String displayName = context.getProperty(DISPLAY_NAME);
      ConnectorId rootId = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new YouTubeConnectorCategory(null, context, rootId, null, displayName);
      rootCategory.setSubCategories(getSubCategories());
      rootCategory.setItems(getItems(rootCategory));
    }
    return rootCategory;
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> result = new ArrayList<>();
    query = query.replaceAll("\\*", " ");

    try {
      String channelId = context.getProperty(CHANNEL_ID);
      if (category != null && !category.getConnectorId().isRootId()) {
        YouTubeCacheItem cacheItem = cache.get(category.getConnectorId());
        List<PlaylistItem> playlistItems = cacheItem.getPlaylistItems();
        for (PlaylistItem video : playlistItems) {
          String name = video.getSnippet().getTitle();
          if (name.toLowerCase().contains(query.toLowerCase()) || query.trim().length() == 0) {
            ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), video.getSnippet().getResourceId().getVideoId());
            result.add(getItem(itemId));
          }
        }
      }
      else {
        //ROOT
        SearchListResponse response = getYouTube().search().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).setType(YOUTUBE_VIDEO_TYPE_SNIPPET).setQ(query).execute();
        List<com.google.api.services.youtube.model.SearchResult> searchResults = response.getItems();
        if (searchResults != null) {
          for (com.google.api.services.youtube.model.SearchResult searchResult : searchResults) {
            ConnectorId connectorId = ConnectorId.createItemId(context.getConnectionId(), searchResult.getId().getVideoId());
            if(cache.get(connectorId) != null) {
              ConnectorItem item = getItem(connectorId);
              result.add(item);
            }
            else {
              LOGGER.warn("Video " + searchResult.getSnippet().getTitle() + " not found in youtube cache.");
            }
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Error retrieving videos", e.getMessage(), e);
    }
    return new ConnectorSearchResult<>(result);
  }

  @Override
  public Boolean refresh(@Nonnull ConnectorCategory category) {
    cache.invalidate();
    init(context);
    return true;
  }

  @Override
  public ConnectorItem upload(ConnectorCategory category, String itemName, InputStream inputStream) {
    return null;
  }

  // -------------------- Helper ---------------------------------------------------------------------------------------

  /**
   * Creates the youtube client to as REST client.
   */
  private YouTube getYouTube() throws ConnectorException {
    if (this.youTube == null) {
      try {
        List<String> scopes = Lists.newArrayList(HTTPS_WWW_GOOGLEAPIS_COM_AUTH_YOUTUBE_FORCE_SSL);
        String credentialsJson = context.getProperty(CREDENTIALS_JSON);
        if (credentialsJson == null || credentialsJson.length() == 0) {
          throw new IOException("No credentialsJson found for youtube connector");
        }
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
        if (credential.createScopedRequired()) {
          credential = credential.createScoped(scopes);
        }
        youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("youtubeProvider").build();
      } catch (IOException e) {
        throw new ConnectorException(e);
      }
    }
    return youTube;
  }


  private void rebuildCache() {
    try {
      List<Playlist> playlists = new ArrayList<>();

      String channelId = context.getProperty(CHANNEL_ID);
      if(!StringUtils.isEmpty(channelId)) {
        //find all playlists by channel id
        PlaylistListResponse playlistListResponse = getYouTube().playlists().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).execute();
        playlists.addAll(playlistListResponse.getItems());
      }

      String user = context.getProperty(USER);
      if(!StringUtils.isEmpty(user)) {
        YouTube.Channels.List channelsListByUsernameRequest = getYouTube().channels().list(REQUEST_TYPE_BY_USER);
        channelsListByUsernameRequest.setForUsername(user);

        ChannelListResponse r = channelsListByUsernameRequest.execute();
        List<Channel> items = r.getItems();
        for (Channel item : items) {
          PlaylistListResponse channelPlayListResponse = getYouTube().playlists().list(REQUEST_TYPE_SNIPPET).setChannelId(item.getId()).execute();
          List<Playlist> userPlaylists = channelPlayListResponse.getItems();
          playlists.addAll(userPlaylists);
        }
      }

      if (!playlists.isEmpty()) {
        cache.setPlaylists(playlists);

        //find all playlist items
        for (Playlist playlist : playlists) {
          ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), playlist.getId());
          PlaylistItemListResponse playlistItemsResponse = getYouTube().playlistItems().list(REQUEST_TYPE_SNIPPET).setPlaylistId(playlist.getId()).execute();
          List<PlaylistItem> playlistItems = playlistItemsResponse.getItems();
          if (playlistItems != null) {
            //cache playlist and it's items for category ids
            cache.cache(new YouTubeCacheItem(categoryId, playlist, playlistItems));

            //find all video details
            for (PlaylistItem playlistItem : playlistItems) {
              String videoId = playlistItem.getSnippet().getResourceId().getVideoId();

              VideoListResponse videoListResponse = getYouTube().videos().list(REQUEST_TYPE_SNIPPET + "," + REQUEST_TYPE_STATISTICS).setId(videoId).execute();
              List<Video> videos = videoListResponse.getItems();
              if (videos != null && videos.size() > 0) {
                Video video = videos.get(0);
                ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), video.getId());
                cache.cache(new YouTubeCacheItem(itemId, playlist, playlistItems, null, video));
              }
            }
          }
        }
      }

      //special handling for video on root level
      List<Video> detailledVideos = new ArrayList<>();
      if(!StringUtils.isEmpty(channelId)) {
        SearchListResponse response = getYouTube().search().list(REQUEST_TYPE_SNIPPET).setChannelId(channelId).setType(YOUTUBE_VIDEO_TYPE_SNIPPET).setQ(" ").execute();
        List<SearchResult> searchResults = response.getItems();

        if (searchResults != null) {

          for (SearchResult searchResult : searchResults) {
            String videoId = searchResult.getId().getVideoId();
            ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), videoId);
            //load all the details of the video before caching them
            VideoListResponse videoListResponse = getYouTube().videos().list(REQUEST_TYPE_SNIPPET + "," + REQUEST_TYPE_STATISTICS).setId(videoId).execute();
            List<Video> videos = videoListResponse.getItems();
            if (videos != null && videos.size() > 0) {
              Video video = videos.get(0);
              detailledVideos.add(video);
              cache.cache(new YouTubeCacheItem(itemId, null, null, null, video));
            }
          }
        }
      }
      //now cache the values for the root category too
      ConnectorId rootId = ConnectorId.createRootId(context.getConnectionId());
      cache.cache(new YouTubeCacheItem(rootId, detailledVideos));
    } catch (IOException e) {
      throw new ConnectorException(e);
    }
  }


  /**
   * There are only one kind of subcategories which are the playlists that are children
   * of the root channel, so we don't have to care about the tree relation here
   */
  private List<ConnectorCategory> getSubCategories() {
    List<ConnectorCategory> result = new ArrayList<>();
    List<Playlist> playlists = cache.getPlaylists();
    for (Playlist list : playlists) {
      ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), list.getId());
      String name = list.getSnippet().getTitle();
      YouTubeConnectorCategory channel = new YouTubeConnectorCategory(rootCategory, context, categoryId, list, name);
      channel.setItems(getItems(channel));
      result.add(channel);
    }
    return result;
  }

  /**
   * Helper to find the items for the given category
   */
  private List<ConnectorItem> getItems(ConnectorCategory category) {
    List<ConnectorItem> result = new ArrayList<>();

    YouTubeCacheItem cacheItem = cache.get(category.getConnectorId());
    //note that the root video are stored differently, since they don't belong to a playlist
    if (category.getConnectorId().isRootId()) {
      List<Video> videos = cacheItem.getVideos();
      for (Video video : videos) {
        ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), video.getId());
        result.add(getItem(itemId));
      }
    }
    else {
      List<PlaylistItem> playlistItems = cacheItem.getPlaylistItems();
      for (PlaylistItem playlistItem : playlistItems) {
        ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), playlistItem.getSnippet().getResourceId().getVideoId());
        result.add(getItem(itemId));
      }
    }
    return result;
  }
}
