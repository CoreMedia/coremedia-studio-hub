package com.coremedia.blueprint.connectors.api;

import java.util.Map;

/**
 * Instances of this interface are created when a selection of a connector is made.
 * All meta data are simply collected in a map and displayed in the meta data panel.
 */
public interface ConnectorMetaData {
  Map<String,Object> getMetadata();
}
