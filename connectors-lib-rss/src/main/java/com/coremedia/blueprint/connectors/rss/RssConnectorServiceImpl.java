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
import com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames;
import com.coremedia.cap.content.ContentType;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
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
  public boolean init(@NonNull ConnectorContext context) {
    this.context = context;
    return getFeed(context) != null;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId connectorId) throws ConnectorException {
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
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId connectorId) throws ConnectorException {
    //only 1x category which is root
    if (rootCategory == null) {
      rootCategory = getRootCategory(true);
    }
    return rootCategory;
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
    return getRootCategory(true);
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
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
  public InvalidationResult invalidate(@NonNull ConnectorContext context) {
    InvalidationResult result = new InvalidationResult(context);

    if (this.rootCategory != null) {
      List<String> oldTitles = this.rootCategory.getItems().stream().map(ConnectorItem::getName).collect(Collectors.toList());
      SyndFeed feed = getFeed(context);
      if(feed != null) {
        List<SyndEntry> entries = feed.getEntries();
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
    }

    return result;
  }

  public boolean refresh(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    rootCategory = getRootCategory(true);
    return true;
  }

  //-------------------- Helper ----------------------------------------------------------------------------------------

  private ConnectorItem createRssAsset(SyndEntry entry) {
    ConnectorId id = ConnectorId.createItemId(rootCategory.getConnectorId(), entry.getUri());
    return new RssConnectorItem(rootCategory, context, rootCategory.getFeed(), entry, id);
  }

  private List<ConnectorItem> getItems(@NonNull ConnectorCategory category) throws ConnectorException {
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

      SyndFeed feed = getFeed(context);

      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new RssConnectorCategory(this, context, feed, id);
      rootCategory.setSubCategories(Collections.emptyList());
      rootCategory.setItems(getItems(rootCategory));
      rootCategory.setName(displayName);
    }

    return rootCategory;
  }

  private SyndFeed getFeed(ConnectorContext context) {
    String url = this.context.getProperty(URL);
    try {
      SyndFeedInput input = new SyndFeedInput();
      URL feedSource = new URL(url);

      String proxyHost = context.getProperty(ConnectorPropertyNames.PROXY_HOST);
      String proxyPort = context.getProperty(ConnectorPropertyNames.PROXY_PORT);
      String proxyType = context.getProperty(ConnectorPropertyNames.PROXY_TYPE);

      if (proxyType != null && proxyHost != null && proxyPort != null) {
        Proxy proxy = new Proxy(Proxy.Type.valueOf(proxyType.toUpperCase()), new InetSocketAddress(proxyPort, Integer.parseInt(proxyPort)));
        URLConnection urlConnection = feedSource.openConnection(proxy);
        XmlReader.setDefaultEncoding("utf8");
        XmlReader reader = new XmlReader(urlConnection);
        return input.build(reader);
      }

      XmlReader.setDefaultEncoding("utf8");
      XmlReader reader = new XmlReader(feedSource);
      return input.build(reader);
    } catch (Exception e) {
      String plainXML = getFeedXML(url);
      LOGGER.error("Error reading RSS stream '" + url + "': " + e.getMessage() + ", feed XML:\n" + plainXML);
    }
    return null;
  }

  /**
   * Just a helper to detect what went wrong when parsing RSS feed.
   * E.g. the result may be a 301 moved permanently
   */
  private String getFeedXML(String url) {
    StringBuilder response = new StringBuilder();
    try {
      URLConnection connection = new URL(url).openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine = null;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
        response.append(System.getProperty("line.separator"));
      }
      in.close();
    } catch (Exception e) {
      //ignore
    }
    return response.toString();
  }
}
