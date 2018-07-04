package com.coremedia.blueprint.connectors.library;

import com.coremedia.blueprint.connectors.api.ConnectorColumnValue;

/**
 * Default implementation of a connector column value
 */
public class DefaultConnectorColumnValue implements ConnectorColumnValue {
  private String value;
  private String dataIndex;
  private String iconText;
  private String iconTooltip;
  private boolean isIcon;

  public DefaultConnectorColumnValue(String value, String dataIndex, String iconText, String iconTooltip) {
    this.value = value;
    this.dataIndex = dataIndex;
    this.isIcon = true;
    this.iconText = iconText;
    this.iconTooltip = iconTooltip;
  }

  public DefaultConnectorColumnValue(String value, String dataIndex) {
    this.value = value;
    this.dataIndex = dataIndex;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public boolean isIcon() {
    return isIcon;
  }

  @Override
  public String getDataIndex() {
    return dataIndex;
  }

  @Override
  public String getIconText() {
    return iconText;
  }

  @Override
  public String getIconTooltip() {
    return iconTooltip;
  }
}
