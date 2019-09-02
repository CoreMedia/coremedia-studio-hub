package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.impl.ConnectorType;
import com.coremedia.blueprint.connectors.impl.Connectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * A resource to retrieve the basic information of all connectors.
 */
@RestController
@RequestMapping(value = "connector/connectors/", produces = APPLICATION_JSON_VALUE)
public class ConnectorsResource {

  private Connectors connector;

  public ConnectorsResource(Connectors connector) {
    this.connector = connector;
  }

  @GetMapping("types")
  public List<ConnectorType> getRepresentation(@Context HttpServletRequest request) {
    return connector.getConnectorTypes();
  }

  public void setConnector(Connectors connector) {
    this.connector = connector;
  }

}
