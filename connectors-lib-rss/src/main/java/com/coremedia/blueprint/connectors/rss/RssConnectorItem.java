package com.coremedia.blueprint.connectors.rss;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumnValue;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumnValue;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RssConnectorItem extends RssConnectorEntity implements ConnectorItem {
  private SyndEntry rssEntry;

  RssConnectorItem(ConnectorCategory parent, ConnectorContext context, SyndFeed feed, SyndEntry entry, ConnectorId connectorId) {
    super(parent, context, feed, connectorId);
    this.rssEntry = entry;
    setName(entry.getTitle());
  }

  @NonNull
  @Override
  public String getItemType() {
    return "rss";
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @Override
  public List<ConnectorColumnValue> getColumnValues() {
    return Arrays.asList(new DefaultConnectorColumnValue(rssEntry.getAuthor(), "author"));
  }

  @Nullable
  @Override
  public String getThumbnailUrl() {
    List<String> imageUrls = getImageUrls();
    if(!imageUrls.isEmpty()) {
      return imageUrls.get(0);
    }
    return null;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public Date getLastModified() {
    Date date = rssEntry.getUpdatedDate();
    if (date == null) {
      date = rssEntry.getPublishedDate();
    }
    if (date == null) {
      date = getFeed().getPublishedDate();
    }
    return date;
  }

  @Nullable
  @Override
  public String getOpenInTabUrl() {
    return rssEntry.getLink();
  }

  @Nullable
  @Override
  public String getDescription() {
    if (rssEntry.getDescription() != null) {
      return rssEntry.getDescription().getValue();
    }
    return rssEntry.getTitle();
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    String value = getDescription();
    if (value != null) {
      value = value.replaceAll("\\<.*?>", "");
    }
    else {
      value = "";
    }

    StringBuilder builder = new StringBuilder(value);

    List<String> urls = getImageUrls();
    for (String url : urls) {
      builder.append("<br>");
      builder.append("<br>");
      builder.append("<img src=\"" + url + "\" style=\"width:inherit;height:100%;\" alt=\"" + url + "\"/>");
    }
    return "<span style=\"width:100%\">" + builder.toString() + "</span>";
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    data.put("author", rssEntry.getAuthor());
    data.put("url", rssEntry.getLink());
    data.put("title", rssEntry.getTitle());
    data.put("source", rssEntry.getSource());

    List<String> urls = getImageUrls();
    int index = 0;
    for (String url : urls) {
      index++;
      data.put("Image " + index, url);
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

  List<String> getImageUrls() {
    return RssImageExtractor.extractImageUrls(rssEntry);
  }

  SyndEntry getRssEntry() {
    return rssEntry;
  }
}
