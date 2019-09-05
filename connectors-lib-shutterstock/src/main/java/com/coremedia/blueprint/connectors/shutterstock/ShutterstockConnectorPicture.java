package com.coremedia.blueprint.connectors.shutterstock;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.shutterstock.rest.Picture;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShutterstockConnectorPicture extends ShutterstockConnectorEntity implements ConnectorItem {

  private final ShutterstockConnectorServiceImpl service;
  private Picture picture;

  ShutterstockConnectorPicture(ShutterstockConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, Picture picture) {
    super(parent, context, connectorId);
    this.service = service;
    this.picture = picture;
    setName(picture.getId());
  }

  @Override
  public Date getLastModified() {
    return null;
  }

  @Nullable
  @Override
  public String getThumbnailUrl() {
    return picture.getAssets().getPreview().getUrl();
  }

  @NonNull
  @Override
  public String getItemType() {
    return "picture";
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return "https://www.shutterstock.com/de/image-photo/" + picture.getId();
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    return MessageFormat.format("<img style=\"max-width:100%;\" src=\"{0}\" />", picture.getAssets().getHugeThumb().getUrl());
  }

  @Nullable
  @Override
  public String getOpenInTabUrl() {
    return picture.getAssets().getPreview().getUrl();
  }

  @Nullable
  @Override
  public String getDescription() {
    return picture.getDescription();
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();

    data.put("desription", getDescription());
    data.put("imageType", picture.getImageType());
    data.put("mediaType", picture.getMediaType());
    data.put("keywords", String.join(", ", picture.getKeywords()));

    return () -> data;
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return null;
  }
}
