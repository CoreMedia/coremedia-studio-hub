package com.coremedia.blueprint.connectors.rss;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.cap.content.ContentType;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.DISPLAY_NAME;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.URL;

public class RssConnectorServiceImpl implements ConnectorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RssConnectorServiceImpl.class);

  private ConnectorContext context;
  private RssConnectorCategory rootCategory;

  @Override
  public boolean init(@Nonnull ConnectorContext context) {
    this.context = context;
    return getFeed() != null;
  }

  @Override
  public Boolean refresh(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category) {
    rootCategory = getRootCategory(true);
    return true;
  }

  @Override
  public ConnectorItem upload(@Nonnull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    return null;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId connectorId) throws ConnectorException {
    List<ConnectorItem> items = getRootCategory(false).getItems();
    for (ConnectorItem item : items) {
      if (item.getConnectorId().equals(connectorId)) {
        return item;
      }
    }
    return null;
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId connectorId) throws ConnectorException {
    //only 1x category which is root
    if (rootCategory == null) {
      rootCategory = getRootCategory(true);
    }
    return rootCategory;
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    return getRootCategory(true);
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();
    if (searchType == null || searchType.equals(ConnectorItem.DEFAULT_TYPE) || searchType.equals(ContentType.CONTENT)) {

      RssConnectorCategory rootCategory = getRootCategory(false);
      SyndFeed feed = rootCategory.getFeed();
      List<SyndEntry> entries = feed.getEntries();
      for (SyndEntry entry : entries) {
        if (query.equals("*")) {
          results.add(createRssAsset(entry));
          continue;
        }

        SyndContent description = entry.getDescription();
        if (description != null && description.getValue() != null && description.getValue().toLowerCase().contains(query.toLowerCase())) {
          results.add(createRssAsset(entry));
        }
        else if (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(query.toLowerCase())) {
          results.add(createRssAsset(entry));
        }
      }
    }

    return new ConnectorSearchResult<>(results);
  }

  @Override
  public InvalidationResult invalidate(@Nonnull ConnectorContext context) {
    InvalidationResult result = new InvalidationResult(context);

    if (this.rootCategory != null) {
      List<String> oldTitles = this.rootCategory.getItems().stream().map(ConnectorItem::getName).collect(Collectors.toList());
      List<SyndEntry> entries = getFeed().getEntries();
      int count = 0;
      for (SyndEntry entry : entries) {
        ConnectorItem item = createRssAsset(entry);
        if (!oldTitles.contains(item.getName())) {
          count++;
        }
      }

      if (count > 0) {
        getRootCategory(true);
        result.addMessage("rss", rootCategory, Arrays.asList(rootCategory.getName(), count));
        result.addEntity(rootCategory);
      }
      LOGGER.info("'" + this.context.getConnectionId() + "' invalidation found " + count + " new elements.");
    }

    return result;
  }

  //-------------------- Helper ----------------------------------------------------------------------------------------

  private ConnectorItem createRssAsset(SyndEntry entry) {
    ConnectorId id = ConnectorId.createItemId(context.getConnectionId(), entry.getUri());
    return new RssConnectorItem(rootCategory, context, rootCategory.getFeed(), entry, id);
  }

  private List<ConnectorItem> getItems(@Nonnull ConnectorCategory category) throws ConnectorException {
    List<ConnectorItem> result = new ArrayList<>();
    RssConnectorCategory rssConnectorCategory = (RssConnectorCategory) category;
    SyndFeed feed = rssConnectorCategory.getFeed();
    List<SyndEntry> entries = feed.getEntries();
    for (SyndEntry entry : entries) {
      result.add(createRssAsset(entry));
    }
    return result;
  }

  private RssConnectorCategory getRootCategory(boolean refresh) {
    if (rootCategory == null || refresh) {
      String displayName = context.getProperty(DISPLAY_NAME);

      String url = context.getProperty(URL);
      if (StringUtils.isEmpty(displayName)) {
        displayName = url;
      }

      SyndFeed feed = getFeed();

      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new RssConnectorCategory(context, feed, id);
      rootCategory.setSubCategories(Collections.emptyList());
      rootCategory.setItems(getItems(rootCategory));
      rootCategory.setName(displayName);
    }

    return rootCategory;
  }

  private SyndFeed getFeed() {
    String url = context.getProperty(URL);
    try {
      URL feedSource = new URL(url);
      XmlReader.setDefaultEncoding("utf8");
      XmlReader reader = new XmlReader(feedSource);
      SyndFeedInput input = new SyndFeedInput();
      return input.build(reader);
    } catch (Exception e) {
      LOGGER.error("Error reading RSS stream '" + url + "': " + e.getMessage(), e);
    }
    return null;
  }
}
