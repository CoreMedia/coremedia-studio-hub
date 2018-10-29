package com.coremedia.blueprint.connectors.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Common interface to be implemented by category/folder objects of
 * the corresponding system.
 * A category consists of sub categories and child items.
 */
public interface ConnectorCategory extends ConnectorEntity {

  /**
   * This string is used during a search, e.g. when 'Folder' is selected
   * inside the library, this value is passed as search type.
   */
  String DEFAULT_TYPE = "folder";

  @NonNull
  List<ConnectorCategory> getSubCategories();

  /**
   * Returns a list of child items for this category.
   */
  @NonNull
  List<ConnectorItem> getItems();

  /**
   * Optional method that can be implemented to provide a custom column model inside the Studio library.
   * The given column models will be shown in the the order they are added to the list.
   */
  @NonNull
  default List<ConnectorColumn> getColumns() {
    return Collections.emptyList();
  }

  /**
   * Indicates if bulk uploads are supported.
   * @return true if uploads are enabled for this category
   */
  boolean isWriteable();

  /**
   * Returns true if the connector implementation is able
   * to deal with content items dropped on categories.
   */
  default boolean isContentUploadEnabled() {
    return isWriteable();
  }

  /**
   * Optional method to be overwritten when a custom label
   * should be applied for the a category in the library.
   *
   * @return the category type label suffix, will be looked up as category_type_<TYPE>_name inside the resource bundle
   */
  default String getType() {
    return DEFAULT_TYPE;
  }

  /**
   * Called to refresh the service
   * @return true if the operation was successful
   */
  default boolean refresh(@NonNull ConnectorContext context) {
    return true;
  }

  /**
   * Uses the given InputStream and name to create a new connector item in
   * the selected category or folder
   * @param itemName the name of the new connector item
   * @param inputStream the upload input stream
   * @return the newly created item created from the stream or null if not upload is provided
   */
  @Nullable
  default ConnectorItem upload(@NonNull ConnectorContext context, String itemName, InputStream inputStream) {
    return null;
  }
}
