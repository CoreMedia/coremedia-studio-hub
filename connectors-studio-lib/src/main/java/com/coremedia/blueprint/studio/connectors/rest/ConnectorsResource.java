package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.impl.ConnectorType;
import com.coremedia.blueprint.connectors.impl.Connectors;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * A resource to retrieve the basic information of all connectors.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("connector/connectors/")
public class ConnectorsResource {
  private Connectors connector;

  @GET
  @Path("types")
  public List<ConnectorType> getRepresentation(@Context HttpServletRequest request) {
    return connector.getConnectorTypes();
  }

  @Required
  public void setConnector(Connectors connector) {
    this.connector = connector;
  }
}
