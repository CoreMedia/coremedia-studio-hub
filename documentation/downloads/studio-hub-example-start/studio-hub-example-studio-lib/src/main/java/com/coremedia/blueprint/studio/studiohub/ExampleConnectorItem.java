package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Date;

/**
 *
 */
public class ExampleConnectorItem implements ConnectorItem {
  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getDescription() {
    return null;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return null;
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
    return null;
  }

  @Nonnull
  @Override
  public String getName() {
    return null;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return null;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return null;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return null;
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
    return null;
  }

  @Override
  public Boolean delete() {
    return null;
  }
}
