package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ExampleConnectorItem implements ConnectorItem {

  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private ConnectorCategory category;

  public ExampleConnectorItem(ConnectorId id, ConnectorContext context, String name, ConnectorCategory category) {
    this.id = id;
    this.context = context;
    this.name = name;
    this.category = category;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getDescription() {
    return null;
  }

  @Nonnull
  @Override
  public String getItemType() {
    if(id.getExternalId().startsWith("article:")) {
      return "article";
    }
    return ConnectorItem.super.getItemType();
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    if(id.getExternalId().startsWith("article:")) {
      return "This is the <i>content</i> of the <b>external<b> article!";
    }
    return ConnectorItem.super.getPreviewHtml();
  }

  @Nullable
  @Override
  public InputStream stream() {
    try {
      URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1c/FuBK_testcard_vectorized.svg/2000px-FuBK_testcard_vectorized.svg.png");
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
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    data.put("author", "it' me!");
    data.put("source", "Wikipedia");
    return () -> data;
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }

  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return category;
  }

  @Nonnull
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
  public Boolean isDeleteable() {
    return false;
  }

  @Override
  public Boolean delete() {
    return false;
  }
}
