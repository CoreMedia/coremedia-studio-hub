package com.coremedia.blueprint.connectors.api;

import com.coremedia.cap.content.ContentType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Describes which properties to use from CoreMedia content to create connector items.
 */
public interface ConnectorContentUploadTypes {

  /**
   * Returns all content mapping properties as map
   */
  @Nonnull
  Map<String,Object> getProperties();

  /**
   * Returns the content type property that should be read
   * @param  contentType the content type name
   * @return null or a property to read from
   */
  @Nullable
  String getContentProperty(ContentType contentType);
}
