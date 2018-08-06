package com.coremedia.blueprint.connectors.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Common super interface for connection categories and items.
 */
public interface ConnectorEntity {

  @NonNull
  ConnectorId getConnectorId();

  @NonNull
  String getName();

  @NonNull
  ConnectorContext getContext();

  @Nullable
  ConnectorCategory getParent();

  @NonNull
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

  /**
   * Default implementation for preview HTML.
   * There are no default templates provided here as we assume that
   * most categories won't need a preview
   * @return the HTML to render or null if the entity does not support a preview.
   */
  @Nullable
  default String getPreviewHtml() {
    return null;
  }

  /**
   * Returns the thumbnail URL used to show
   * an image preview in the Studio's thumbnail view
   */
  @Nullable
  default String getThumbnailUrl() {
    return null;
  }

  /**
   * Returns true if it is possible to delete the given item or category.
   */
  boolean isDeleteable();

  /**
   * Method to be implemented when a connector item or category should be deleted.
   * @return true if the deletion operation was successful.
   */
  boolean delete();

  /**
   * Optional additional metadata that will be used for the metadata-preview-panel in the library.
   * @return the metadata of the item or null if no additional metadata is available.
   */
  @Nullable
  default ConnectorMetaData getMetaData() {
    return null;
  }

  /**
   * Returns the custom column values for this connector item.
   */
  @NonNull
  default List<ConnectorColumnValue> getColumnValues() {
    return Collections.emptyList();
  }
}
