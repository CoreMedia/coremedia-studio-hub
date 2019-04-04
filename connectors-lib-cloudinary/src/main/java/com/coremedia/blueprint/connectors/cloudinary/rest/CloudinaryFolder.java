package com.coremedia.blueprint.connectors.cloudinary.rest;

import java.util.Date;
import java.util.Map;

/**
 *
 */
public class CloudinaryFolder extends CloudinaryEntity {
  private String name;
  private String path;

  public CloudinaryFolder(Map folderData) {
    this.name = (String) folderData.get("name");
    this.path = (String) folderData.get("path");
  }

  public CloudinaryFolder(String path, String name) {
    this.name = name;
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public Date getLastModified() {
    return null;
  }

  public String getFolder() {
    return path;
  }

  public String getParentFolder() {
    if(path.contains("/")) {
      String parent = path;
      if(path.endsWith("/")) {
        path = path.substring(0, path.lastIndexOf("/"));
      }

      parent = path.substring(0, path.lastIndexOf('/'));
      return parent;
    }
    return null;
  }

  @Override
  public String toString() {
    return "CloudinaryFolder (" + path + ")";
  }
}
