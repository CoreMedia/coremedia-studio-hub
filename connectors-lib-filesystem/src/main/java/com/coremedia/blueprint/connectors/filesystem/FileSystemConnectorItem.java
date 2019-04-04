package com.coremedia.blueprint.connectors.filesystem;


import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.ConnectorMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

public class FileSystemConnectorItem extends FileSystemConnectorEntity implements ConnectorItem {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemConnectorItem.class);

  FileSystemConnectorItem(ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, File file) {
    super(parent, context, connectorId, file);
  }

  @Override
  public long getSize() {
    return getFile().length();
  }

  @Nullable
  @Override
  public String getDescription() {
    return null;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return () -> {
      Map<String, Object> result = new TreeMap<>();
      result.put("path", getFile().getPath());
      return result;
    };
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @Nullable
  @Override
  public InputStream stream() {
    try {
      return new FileInputStream(getFile());
    } catch (FileNotFoundException e) {
      LOGGER.error("Failed to open file asset input stream: " + e.getMessage(), e);
    }
    return null;
  }
}
