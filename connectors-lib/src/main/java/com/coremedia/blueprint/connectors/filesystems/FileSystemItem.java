package com.coremedia.blueprint.connectors.filesystems;

import java.util.List;

/**
 * The item to cache for a remote file system.
 */
public class FileSystemItem<T> {

  private T folderData;
  private List<T> folderItemsData;

  public FileSystemItem(T folderData, List<T> folderItemsData) {
    this.folderData = folderData;
    this.folderItemsData = folderItemsData;
  }

  public T getFolderData() {
    return folderData;
  }

  public void setFolderData(T folderData) {
    this.folderData = folderData;
  }

  public List<T> getFolderItemsData() {
    return folderItemsData;
  }

  public void setFolderItemsData(List<T> folderItemsData) {
    this.folderItemsData = folderItemsData;
  }
}
