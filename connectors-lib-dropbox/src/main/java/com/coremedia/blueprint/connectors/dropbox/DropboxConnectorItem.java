package com.coremedia.blueprint.connectors.dropbox;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

public class DropboxConnectorItem extends DropboxConnectorEntity implements ConnectorItem {

  private FileMetadata fileMetadata;

  DropboxConnectorItem(DropboxConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Metadata metadata, ConnectorId connectorId) {
    super(service, parent, context, metadata, connectorId);
    this.fileMetadata = (FileMetadata) metadata;
  }

  @Override
  public long getSize() {
    return fileMetadata.getSize();
  }

  @Override
  public Date getLastModified() {
    return fileMetadata.getClientModified();
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    String managementUrl = parent.getManagementUrl();
    return managementUrl + "?preview=" + fileMetadata.getName();
  }

  @Nullable
  @Override
  public String getDescription() {
    return fileMetadata.getPathDisplay();
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return HashMap::new;
  }

  @Override
  public boolean isDeleteable() {
    return true;
  }

  @Override
  public boolean delete() {
    return service.delete(this);
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return service.stream(this);
  }
}
