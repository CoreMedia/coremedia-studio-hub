package com.coremedia.blueprint.connectors.library;

import com.coremedia.blueprint.connectors.api.ConnectorColumn;

import javax.annotation.Nonnull;

/**
 * A default implementation of a connector column.
 */
public class DefaultConnectorColumn implements ConnectorColumn {

  private String title;
  private String dataIndex;
  private int width;
  private int index;

  public DefaultConnectorColumn(String title, String dataIndex, int width, int index) {
    this.title = title;
    this.dataIndex = dataIndex;
    this.width = width;
    this.index = index;
  }

  public DefaultConnectorColumn(String title, String dataIndex, int width) {
    this(title, dataIndex, width, -1);
  }

  @Nonnull
  @Override
  public String getTitle() {
    return title;
  }

  @Nonnull
  @Override
  public String getDataIndex() {
    return dataIndex;
  }

  @Override
  public boolean isSortable() {
    return true;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }

  @Override
  public boolean isHideable() {
    return true;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getIndex() {
    return index;
  }
}
