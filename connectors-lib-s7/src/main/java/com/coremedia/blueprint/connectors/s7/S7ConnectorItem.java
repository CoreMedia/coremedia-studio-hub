package com.coremedia.blueprint.connectors.s7;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class S7ConnectorItem extends S7ConnectorEntity implements ConnectorItem {

  public S7ConnectorItem(ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, S7Container file) {
    super(parent, context, connectorId, file);
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getUrl() {
    return super.getUrl();
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
      Map<String, Object> metaData = new HashMap<>();
      metaData.put(ConnectorPropertyNames.URL, getUrl());
      return metaData;
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
      return new URL(getUrl()).openStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
