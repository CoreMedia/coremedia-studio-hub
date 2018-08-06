package com.coremedia.blueprint.connectors.api;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Interface for the content service which manages the lookup of duplicates, content creation and
 * post-processing.
 */
public interface ConnectorContentService {

  /**
   * Creates content for the connector item with the given id.
   * The connector id will be stored inside the content.
   * @param connectorId the connector id to identify the ConnectorItem instance
   * @param folder the folder to create the new content into
   * @param site the site that is used to avoid duplicate content creation
   * @return
   */
  @Nullable
  Content createContent(@NonNull ConnectorId connectorId, @NonNull String folder, @Nullable Site site);

  /**
   * Tries to find the content that has been created for the given connector id
   * Returns null if no such content was found.
   * @param entity the connector entity that is stored in the created content, if exists
   * @param folder the target folder used to search for a matching subfolder
   * @param site the site used as search filter for the content or null for a global lookup
   */
  @Nullable
  Content findContent(@NonNull ConnectorEntity entity, @Nullable String folder, @Nullable Site site);

  /**
   * Triggers the (asynchronous) post processing of content.
   * The post processing trigger the applicable list of write interceptors
   * that load data and information from the external systems and fill the content with it.
   * @param content the content to be filled by the write interceptors
   * @param entity the entity that contains the external system data
   * @param wait true to wait until the post processing is finished.
   */
  void processContent(@NonNull Content content, @NonNull ConnectorEntity entity, boolean wait);

  /**
   * Applies the connectorId to the localSettings struct
   * @param content the content to apply the id for
   * @param id      the id to set
   */
  void setConnectorId(@NonNull Content content, @NonNull ConnectorId id);
}
