package com.coremedia.blueprint.connectors.api;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The Column interface to be implemented for a custom column inside the library view.
 */
public interface ConnectorColumn {
  /**
   * The title of the library, can return the actual label
   * or a resource bundle key of value that has been overridden in the ConnectorsStudioPlugin.properties.
   */
  @NonNull
  String getTitle();

  /**
   * The dataIndex identifies the value of the item that is rendered by this column
   */
  @NonNull
  String getDataIndex();

  /**
   * Returns the position of the column. By default every additional column will be added behind the name column.
   */
  default int getIndex() {
    return -1;
  }

  /**
   * Returns true if this column is sortable
   */
  boolean isSortable();

  /**
   * Returns true if this column is resizeable
   */
  boolean isResizeable();

  /**
   * Returns true if this column can be hidden.
   */
  boolean isHideable();

  /**
   * Returns the widths of the column
   */
  int getWidth();
}
