package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.impl.ConnectorContextImpl;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.studio.connectors.rest.model.ConnectorModel;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorEntityRepresentation;
import com.coremedia.rest.linking.EntityResource;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract public class ConnectorEntityResource<Entity extends ConnectorEntity> implements EntityResource<Entity> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorEntityResource.class);

  protected static final String ID = "id";

  private String decodedId;
  private ConnectorContextProvider connectorContextProvider;
  private Connectors connector;

  public Entity getEntity() {
    return doGetEntity();
  }

  protected abstract Entity doGetEntity();

  public void setEntity(Entity entity) {
    setId(entity.getConnectorId().toString());
  }

  public void setId(@Nullable String id) {
    if (id != null) {
      this.decodedId = URLDecoder.decode(id, StandardCharsets.UTF_8);
    }
  }

  public String getId() {
    if (decodedId != null) {
      return URLEncoder.encode(decodedId, StandardCharsets.UTF_8);
    }
    return null;
  }

  protected String getDecodedId() {
    return decodedId;
  }

  @GetMapping
  public ConnectorEntityRepresentation get(@PathVariable(ID) String id) {
    try {
      setId(id);

      return getRepresentation();
    } catch (URISyntaxException e) {
      LOGGER.error("Error creating entity representation " + getEntity() + ": " + e.getMessage(), e);
    }
    return null;
  }


  @DeleteMapping
  public boolean delete() {
    ConnectorEntity entity = getEntity();
    return entity.delete();
  }


  protected void fillRepresentation(ConnectorEntity entity, ConnectorEntityRepresentation representation) {
    representation.setDeleteable(entity.isDeleteable());

    //fill data
    ConnectorContextImpl context = (ConnectorContextImpl) getContext(entity.getConnectorId());
    String siteId = context.getSiteId();
    if (siteId == null) {
      siteId = ConnectorResource.siteDefault;
    }
    ConnectorModel model = new ConnectorModel(getContext(entity.getConnectorId()).getType(), siteId, Collections.emptyList());
    representation.setConnector(model);
    representation.setConnectorType(model.getConnectorType());

    //fill entity specific data
    representation.setConnectorId(entity.getConnectorId());
    representation.setDisplayName(entity.getDisplayName());
    representation.setName(entity.getName());
    representation.setParent(entity.getParent());
    representation.setLastModified(entity.getLastModified());
    representation.setManagementUrl(entity.getManagementUrl());
    representation.setThumbnailUrl(entity.getThumbnailUrl());
  }

  protected abstract ConnectorEntityRepresentation getRepresentation() throws URISyntaxException;

  protected ConnectorContext getContext(ConnectorId id) {
    return connectorContextProvider.createContext(id.getConnectionId());
  }

  protected ConnectorConnection getConnection(ConnectorId id) {
    return connector.getConnection(getContext(id));
  }

  @Autowired
  public void setConnector(Connectors connector) {
    this.connector = connector;
  }

  public Connectors getConnector() {
    return connector;
  }

  public ConnectorContextProvider getConnectorContextProvider() {
    return connectorContextProvider;
  }

  @Autowired
  public void setConnectorContextProvider(ConnectorContextProvider connectorContextProvider) {
    this.connectorContextProvider = connectorContextProvider;
  }

  @NonNull
  @Override
  public Map<String, String> getPathVariables(@NonNull Entity entity) {
    Map<String, String> vars = new HashMap<>();
    String id = entity.getConnectorId().toString();
    vars.put("id", URLEncoder.encode(id, StandardCharsets.UTF_8));
    return vars;
  }

}
