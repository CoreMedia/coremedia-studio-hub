package com.coremedia.blueprint.connectors.api;

import com.coremedia.cap.content.ContentType;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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

  /**
   * Returns the content type property that should be read
   * @param  contentType the content type name
   * @return null or a property to read from
   */
  @Nullable
  String getContentProperty(ContentType contentType);
}
