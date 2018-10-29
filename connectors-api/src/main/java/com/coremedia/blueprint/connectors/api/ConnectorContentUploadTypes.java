package com.coremedia.blueprint.connectors.api;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Map;

/**
 * Describes which properties to use from CoreMedia content to create connector items.
 * This mapping is used when you drop content from the repository list to a connector category.
 * By default, all writeable connector types will then use these properties from the content
 * to create an InputStream for uploading the content data to the external system.
 */
public interface ConnectorContentUploadTypes {

  /**
   * Returns all content mapping properties as map
   */
  @NonNull
  Map<String,Object> getProperties();
}
