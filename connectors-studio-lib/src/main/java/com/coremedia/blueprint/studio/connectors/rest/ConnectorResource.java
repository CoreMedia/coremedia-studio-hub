package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.studio.connectors.rest.model.ConnectorModel;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorRepresentation;
import com.coremedia.rest.linking.EntityResource;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.coremedia.blueprint.studio.connectors.rest.ConnectorResource.CONNECTOR_TYPE;
import static com.coremedia.blueprint.studio.connectors.rest.ConnectorResource.SITE_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * A resource to retrieve the basic information of a connector.
 */
@RestController
@RequestMapping(value = "connector/connector/{" + CONNECTOR_TYPE + "}/{" + SITE_ID + "}", produces = APPLICATION_JSON_VALUE)
public class ConnectorResource implements EntityResource<ConnectorModel> {

  static final String CONNECTOR_TYPE = "connectorType";
  static final String SITE_ID = "siteId";

  private Connectors connector;

  public static final String siteDefault = "all";

  public ConnectorResource(Connectors connectors) {
    this.connector = connectors;
  }

  @GetMapping()
  public ConnectorRepresentation getRepresentation(@PathVariable(CONNECTOR_TYPE) String connectorType, @PathVariable(SITE_ID) Optional<String> siteId) {
    ConnectorRepresentation connectorRepresentation = new ConnectorRepresentation();
    ConnectorModel entity = getEntity(connectorType, siteId.orElse(siteDefault));
    connectorRepresentation.setName(entity.getName());
    connectorRepresentation.setConnectorType(entity.getConnectorType());
    connectorRepresentation.setConnections(entity.getConnections());
    connectorRepresentation.setItemTypes(entity.getItemTypes());
    connectorRepresentation.setSiteId(entity.getSiteId());
    return connectorRepresentation;
  }

  public ConnectorModel getEntity(String connectorType, String siteId) {
    String targetSiteId = siteId;
    if (targetSiteId.equals(siteDefault)) {
      targetSiteId = null;
    }
    List<ConnectorConnection> connectionsByType = connector.getConnectionsByType(connectorType, targetSiteId);
    return new ConnectorModel(connectorType, siteId, connectionsByType);
  }

  public void setConnector(Connectors connector) {
    this.connector = connector;
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull ConnectorModel entity) {
    Map<String, String> vars = new HashMap<>();
    vars.put(CONNECTOR_TYPE, entity.getConnectorType());
    vars.put(SITE_ID, entity.getSiteId());
    return vars;
  }
}
