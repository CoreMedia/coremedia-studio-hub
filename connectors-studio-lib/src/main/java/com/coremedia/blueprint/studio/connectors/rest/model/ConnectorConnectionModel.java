package com.coremedia.blueprint.studio.connectors.rest.model;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContentUploadTypes;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.impl.ConnectorContextImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ConnectorConnectionModel {
  private String connectorType;
  private String connectionId;
  private ConnectorCategory rootCategory;
  private Map<String, Object> context = new HashMap<>();

  public ConnectorConnectionModel(ConnectorConnection connection) {
    connectorType = connection.getConnectorType();
    connectionId = connection.getContext().getConnectionId();
    rootCategory = connection.getConnectorService().getRootCategory(connection.getContext());

    //we do create a private context here since we only want to reveal client relevant information
    ConnectorContext privateContext = connection.getContext();
    //the connection is mandatory!
    context.put(ConnectorContextImpl.CONNECTION_ID, privateContext.getConnectionId());
    context.put(ConnectorContextImpl.CONTENT_SCOPE, privateContext.getContentScope());
    context.put(ConnectorContextImpl.MARK_AS_READ, privateContext.isMarkAsReadEnabled());
    context.put(ConnectorContextImpl.PREVIEW_THRESHOLD, privateContext.getPreviewThresholdMB());
    context.put(ConnectorContextImpl.TYPE, privateContext.getType());
    context.put(ConnectorContextImpl.DATE_FORMAT, privateContext.getDateFormat());
    context.put(ConnectorContextImpl.DEFAULT_COLUMNS, privateContext.getDefaultColumns());

    ConnectorContentUploadTypes contentUploadTypes = privateContext.getContentUploadTypes();
    Map<String, List<String>> uploadTypeMapping = new HashMap<>();

    if(contentUploadTypes != null) {
      Set<String> contentTypes = contentUploadTypes.getProperties().keySet();
      for (String contentType : contentTypes) {
        uploadTypeMapping.put(contentType, contentUploadTypes.getPropertyNames(contentType));
      }
    }

    context.put(ConnectorContextImpl.CONTENT_UPLOAD_TYPES, uploadTypeMapping);
  }

  public String getConnectorType() {
    return connectorType;
  }

  public ConnectorCategory getRootCategory() {
    return rootCategory;
  }

  public Map<String, Object> getContext() {
    return context;
  }

  public String getConnectionId() {
    return connectionId;
  }
}
