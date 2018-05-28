package com.coremedia.blueprint.connectors.cloudinary;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 *
 */
abstract public class CloudinaryConnectorEntity implements Serializable, ConnectorEntity {
  protected final static String FOLDER_BASE_URL = "https://cloudinary.com/console/media_library/folders/";
  protected final static String ASSET_BASE_URL = "https://cloudinary.com/console/media_library/asset/";
  protected final static String ASSET_TYPE_IMAGES = "images";
  protected final static String ASSET_TYPE_AUDIO_VIDEO = "videos";
  protected final static String ASSET_TYPE_OTHER = "other";

  protected ConnectorId id;
  protected ConnectorContext context;

  CloudinaryConnectorEntity(ConnectorContext context, ConnectorId id) {
    this.id = id;
    this.context = context;
  }

  @Nonnull
  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return context;
  }
}
