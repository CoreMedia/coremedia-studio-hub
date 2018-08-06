package com.coremedia.blueprint.connectors.rss;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Date;

public class RssConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;
  private ConnectorCategory parent;

  private SyndFeed feed;

  RssConnectorEntity(ConnectorCategory parent, ConnectorContext context, SyndFeed feed, ConnectorId connectorId) {
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
    this.name = feed.getTitle();
    this.feed = feed;
  }

  @Override
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  public Date getLastModified() {
    return feed.getPublishedDate();
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  public void setContext(ConnectorContext context) {
    this.context = context;
  }

  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  public void setParent(ConnectorCategory parent) {
    this.parent = parent;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @NonNull
  @Override
  public ConnectorId getConnectorId() {
    return connectorId;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return feed.getLink();
  }

  SyndFeed getFeed() {
    return feed;
  }
}
