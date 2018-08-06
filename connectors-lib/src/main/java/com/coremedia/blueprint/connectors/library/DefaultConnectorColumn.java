package com.coremedia.blueprint.connectors.library;

import com.coremedia.blueprint.connectors.api.ConnectorColumn;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A default implementation of a connector column.
 */
public class DefaultConnectorColumn implements ConnectorColumn {

  private String title;
  private String dataIndex;
  private int width;
  private int index;
  private boolean sortable = true;
  private boolean hideable = true;
  private boolean resizeable = true;

  public DefaultConnectorColumn(String title, String dataIndex, int width, int index) {
    this.title = title;
    this.dataIndex = dataIndex;
    this.width = width;
    this.index = index;
  }

  public DefaultConnectorColumn(String title, String dataIndex, int width) {
    this(title, dataIndex, width, -1);
  }

  @NonNull
  @Override
  public String getTitle() {
    return title;
  }

  @NonNull
  @Override
  public String getDataIndex() {
    return dataIndex;
  }

  @Override
  public boolean isSortable() {
    return sortable;
  }

  @Override
  public boolean isResizeable() {
    return resizeable;
  }

  @Override
  public boolean isHideable() {
    return hideable;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getIndex() {
    return index;
  }


  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }

  public void setHideable(boolean hideable) {
    this.hideable = hideable;
  }

  public void setResizeable(boolean resizeable) {
    this.resizeable = resizeable;
  }
}
