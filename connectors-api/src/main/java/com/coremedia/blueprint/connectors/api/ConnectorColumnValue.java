package com.coremedia.blueprint.connectors.api;

/**
 * The interface to be implemented for column values
 */
public interface ConnectorColumnValue {

  /**
   * The actual value that should be rendered for the column.
   * This value can contain the text that should be displayed or the iconCls
   * when the value is supposed to be an icon.
   */
  String getValue();

  /**
   * Returns true if the column value should be rendered as icon class.
   */
  boolean isIcon();

  /**
   * The data index this column value is for.
   * Ensure that the ConnectorColumn model does support it
   */
  String getDataIndex();

  String getIconText();

  String getIconTooltip();
}
