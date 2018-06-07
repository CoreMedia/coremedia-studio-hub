package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class ExampleConnectorCategory implements ConnectorCategory {

  @Nonnull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return null;
  }

  @Nonnull
  @Override
  public List<ConnectorItem> getItems() {
    return null;
  }

  @Override
  public boolean isWriteable() {
    return false;
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
