package com.coremedia.blueprint.connectors.api;

import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
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
  boolean init(@Nonnull ConnectorContext context) throws ConnectorException;

  /**
   * Terminates the connector connection, ensures a clean refresh
   * when the context properties of a connection have changed.
   */
  default void shutdown(@Nonnull ConnectorContext context) throws ConnectorException {}

  /**
   * Returns the ConnectorItemObject for the given id
   * @param id the ConnectorId which includes the external id
   */
  @Nullable
  ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException;

  /**
   * Returns the complete category, including the children.
   */
  @Nullable
  ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException;

  /**
   * Returns the root category of the connected system which is used
   * as entry point for the Studio library.
   */
  @Nonnull
  ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException;

  /**
   * Returns the list of items to be invalidated by the client.
   * If not invalidation interval is configured, the method will be ignored.
   * @return the list of items that should be ignored.
   */
  default InvalidationResult invalidate(@Nonnull ConnectorContext context) {
    //no default invalidation
    return null;
  }

  @Nonnull
  ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params);

  /**
   * Called to refresh the service
   * @return true if the operation was successful
   */
  Boolean refresh(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category);

  /**
   * Uses the given InputStream and name to create a new connector item in
   * the selected category or folder
   * @param category the category to upload the file to
   * @param itemName the name of the new connector item
   * @param inputStream the upload input stream
   * @return the newly created item created from the stream or null if not upload is provided
   */
  @Nullable
  ConnectorItem upload(@Nonnull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream);
}
