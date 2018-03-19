package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;

/**
 * Common super interface for connection categories and items.
 */
public interface ConnectorEntity {

  ConnectorId getConnectorId();

  String getConnectorType();

  @Nonnull
  String getName();

  @Nonnull
  ConnectorContext getContext();

  @Nullable
  ConnectorCategory getParent();

  @Nonnull
  String getDisplayName();

  /**
   * Returns the last modification date
   */
  @Nullable
  Date getLastModified();

  /**
   * Returns the management URL used to show
   * the asset in the provided connected system user interface.
   */
  @Nullable
  String getManagementUrl();

  Boolean isDeleteable();

  Boolean delete();
}
