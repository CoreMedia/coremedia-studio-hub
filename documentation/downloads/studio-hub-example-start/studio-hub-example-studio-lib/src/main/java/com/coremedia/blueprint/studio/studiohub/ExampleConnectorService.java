package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public class ExampleConnectorService implements ConnectorService {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleConnectorService.class);

  @Override
  public boolean init(@Nonnull ConnectorContext context) throws ConnectorException {
    return false;
  }

  @Override
  public void shutdown(@Nonnull ConnectorContext context) throws ConnectorException {

  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    return null;
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    return null;
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    return null;
  }

  @Override
  public InvalidationResult invalidate(@Nonnull ConnectorContext context) {
    return null;
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    return null;
  }
}
