package com.coremedia.blueprint.connectors.api;

import com.coremedia.cap.content.ContentType;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
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
   * Returns the blob property names for the given content type that should
   * be used when the related content is uploaded to the external system.
   * @param contentType the content type name
   * @return the property names or an empty list if no mapping is defined for the given type
   */
  @NonNull
  List<String> getBlobPropertyNames(@NonNull ContentType contentType);
}
