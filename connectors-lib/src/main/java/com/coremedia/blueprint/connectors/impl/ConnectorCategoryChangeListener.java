package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;

import javax.annotation.Nonnull;

/**
 * This interfaced is used for invalidation.
 * For asynchronous task we have to use an invalidation source
 * to ensure the Studio refresh afterwards.
 */
public interface ConnectorCategoryChangeListener {

  /**
   * To be called when a category has changed.
   */
  void categoryChanged(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category);
}
