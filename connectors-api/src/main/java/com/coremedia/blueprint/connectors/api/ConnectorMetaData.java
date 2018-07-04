package com.coremedia.blueprint.connectors.api;

import java.util.Map;

/**
 * Instances of this interface are created when a selection of a connector is made.
 * All meta data are simply collected in a map and displayed in the meta data panel.
 */
public interface ConnectorMetaData {

  /**
   * The key/value pairs for the metadata.
   * The frontend will improve the formatting depending on the data type
   * and predefined metadata keys.
   */
  Map<String,Object> getMetadata();
}
