package com.coremedia.blueprint.connectors.brightcove;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

/**
 *
 */
public class BrightcoveItem implements ConnectorItem {

  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private ConnectorCategory category;


  private String description;
  private String thumbnailUrl;

  public BrightcoveItem(ConnectorId id, ConnectorContext context, String name, ConnectorCategory category) {
    this.id = id;
    this.context = context;
    this.name = name;
    this.category = category;
  }

  @NonNull
  @Override
  public String getItemType() {
    return "brightcove";
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    String html = ConnectorItem.super.getPreviewHtml();
    if (html != null) {
      return MessageFormat.format(html, getThumbnailUrl());
    }
    return null;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getDescription() {
    return description;
  }

  @Nullable
  @Override
  public InputStream stream() {
    try {
      URL url = new URL(getThumbnailUrl());
      return url.openStream();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Nullable
  @Override
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return null;
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }

  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  @NonNull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return category;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @Override
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }
}
