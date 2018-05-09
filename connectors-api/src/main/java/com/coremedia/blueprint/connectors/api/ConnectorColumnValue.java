package com.coremedia.blueprint.connectors.api;

/**
 * The interface to be implemented for column values
 */
public interface ConnectorColumnValue {
  String getValue();

  boolean isIcon();

  String getDataIndex();

  String getIconText();

  String getIconTooltip();
}
