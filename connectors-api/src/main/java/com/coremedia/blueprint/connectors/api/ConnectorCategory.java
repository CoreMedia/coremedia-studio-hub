package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
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

  @Nonnull
  List<ConnectorCategory> getSubCategories();

  /**
   * Returns a list of child items for this category.
   */
  @Nonnull
  List<ConnectorItem> getItems();

  /**
   * Optional method that can be implemented to provide a custom column model inside the Studio library.
   * The given column models will be shown in the the order they are added to the list.
   */
  @Nonnull
  default List<ConnectorColumn> getColumns() {
    return Collections.emptyList();
  }

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
