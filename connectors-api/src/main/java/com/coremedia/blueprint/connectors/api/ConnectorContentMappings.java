package com.coremedia.blueprint.connectors.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;

/**
 * Contains all information to create CoreMedia content out of items.
 */
public interface ConnectorContentMappings {

  /**
   * Returns all content mapping properties as map
   */
  @NonNull
  Map<String,Object> getProperties();

  /**
   * Returns the content type that should be created for the given connector item type
   * @param type the type name of a connector item
   * @return null or a target content content
   */
  @NonNull
  String get(String type);

  /**
   * Returns the value that is defined as default target content type
   * for any connector item's type that is not in the mapping list.
   */
  @NonNull
  String getDefaultMapping();
}
