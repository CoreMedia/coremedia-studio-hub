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
   * Returns the property names for the given content type that should
   * be used when the related content is uploaded to the external system.
   * Using the content type here will also include the mapping of supertypes.
   * @param contentType the content type name
   * @return the property names or an empty list if no mapping is defined for the given type
   */
  @NonNull
  List<String> getPropertyNames(@NonNull ContentType contentType);

  /**
   * Returns the mapping configuration for the given type name
   * @param contentType the name of the content type
   * @return the list of types that have been mapped
   */
  @NonNull
  List<String> getPropertyNames(@NonNull String contentType);
}
