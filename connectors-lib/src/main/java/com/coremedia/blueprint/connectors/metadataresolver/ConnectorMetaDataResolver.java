package com.coremedia.blueprint.connectors.metadataresolver;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.common.util.Predicate;

import java.io.File;

/**
 * A metadata resolver used a local copy of a connector item to extract metadata from it.
 */
public interface ConnectorMetaDataResolver extends Predicate<ConnectorItem> {

  /**
   * Returns the meta data for the given item.
   * @param item the item to resolve the meta data for
   * @param itemTempFile the tmp file that has been downloaded to analyze the metadata
   */
  ConnectorMetaData resolveMetaData(ConnectorItem item, File itemTempFile);
}
