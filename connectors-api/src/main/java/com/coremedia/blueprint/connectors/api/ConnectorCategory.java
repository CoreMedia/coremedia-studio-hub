package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
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

  @Nonnull
  List<ConnectorCategory> getSubCategories();

  @Nonnull
  List<ConnectorItem> getItems();

  /**
   * Indicates if bulk uploads are supported.
   * @return true if uploads are enabled for this category
   */
  boolean isWriteable();

  /**
   * Optional method to be overwritten when a custom label
   * should be applied for the a category in the library.
   *
   * @return the category type label suffix, will be looked up as category_type_<TYPE>_name inside the resource bundle
   */
  default String getType() {
    return DEFAULT_TYPE;
  }
}
