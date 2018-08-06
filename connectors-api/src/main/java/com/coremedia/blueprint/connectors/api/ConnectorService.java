package com.coremedia.blueprint.connectors.api;

import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;

/**
 * This interface has to be implemented by each connected system library, additionally
 * to the ConnectorItem and ConnectorCategory interface.
 */
public interface ConnectorService {

  /**
   * Initializes the service using the context object
   * which contains all content based configuration values.
   * @param context the context to initialize the service with
   * @return true if the connection initialization was successful
   */
  boolean init(@NonNull ConnectorContext context) throws ConnectorException;

  /**
   * Terminates the connector connection, ensures a clean refresh
   * when the context properties of a connection have changed.
   */
  default void shutdown(@NonNull ConnectorContext context) throws ConnectorException {}

  /**
   * Returns the ConnectorItemObject for the given id
   * @param id the ConnectorId which includes the external id
   */
  @Nullable
  ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException;

  /**
   * Returns the complete category, including the children.
   */
  @Nullable
  ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException;

  /**
   * Returns the root category of the connected system which is used
   * as entry point for the Studio library.
   */
  @NonNull
  ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException;

  /**
   * Returns the list of items to be invalidated by the client.
   * If not invalidation interval is configured, the method will be ignored.
   * @return the list of items that should be ignored.
   */
  default InvalidationResult invalidate(@NonNull ConnectorContext context) {
    //no default invalidation
    return null;
  }

  @NonNull
  ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params);
}
