package com.coremedia.blueprint.connectors.canto.model;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.canto.CantoConnectorServiceImpl;
import com.coremedia.blueprint.connectors.canto.rest.services.MetadataService;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;

public abstract class CantoConnectorEntity {

  private static final String MANAGEMENT_BASE_PATH = "/cwc/catalog";

  ConnectorId connectorId;
  CantoConnectorServiceImpl connectorService;

  CantoConnectorEntity(ConnectorId connectorId, @NonNull CantoConnectorServiceImpl connectorService) {
    this.connectorId = connectorId;
    this.connectorService = connectorService;
  }

  public ConnectorId getConnectorId() {
    return connectorId;
  }

  public CantoConnectorServiceImpl getConnectorService() {
    return connectorService;
  }

  @NonNull
  public ConnectorContext getContext() {
    return connectorService.getContext();
  }

  String getCatalogId() {
    return connectorService.getCatalogId();
  }

  MetadataService getMetadataService() {
    return connectorService.getMetadataService();
  }

  String buildManagementUrl(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(getContext().getProperty("host"))
            .path(MANAGEMENT_BASE_PATH);

    // add resource path
    uriBuilder.path(resourcePath);

    // Add query params
    if (queryParams != null) {
      uriBuilder.queryParams(queryParams);
    }

    UriComponents uriComponents = pathParams != null ? uriBuilder.buildAndExpand(pathParams) : uriBuilder.build();
    return uriComponents.toString();
  }

}
