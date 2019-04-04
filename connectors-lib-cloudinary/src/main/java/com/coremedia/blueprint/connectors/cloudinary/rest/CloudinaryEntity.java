package com.coremedia.blueprint.connectors.cloudinary.rest;

/**
 *
 */
abstract public class CloudinaryEntity {

  public boolean isFolder() {
    return this instanceof CloudinaryFolder;
  }

  abstract public String getName();

  abstract public String getFolder();
}
