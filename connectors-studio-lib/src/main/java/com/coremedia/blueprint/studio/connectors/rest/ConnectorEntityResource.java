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
import com.coremedia.cap.multisite.Site;
import com.coremedia.rest.linking.EntityResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Locale;

abstract public class ConnectorEntityResource<Entity extends ConnectorEntity> implements EntityResource<Entity> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorEntityResource.class);

  private static final String ID = "id";
  static final String SESSION_ATTRIBUTE_LOCALE = "studio-locale";
  static final String SESSION_ATTRIBUTE_PREFERRED_SITE = "studio-preferred-site";

  private String decodedId;
  private ConnectorContextProvider connectorContextProvider;
  private Connectors connector;

  @Override
  public Entity getEntity() {
    return doGetEntity();
  }

  protected abstract Entity doGetEntity();

  @Override
  public void setEntity(Entity entity) {
    setId(entity.getConnectorId().toString());
  }

  @PathParam(ID)
  public void setId(@Nullable String id) {
    try {
      if (id != null) {
        this.decodedId = URLDecoder.decode(id, "utf8");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public String getId() {
    try {
      if (decodedId != null) {
        return URLEncoder.encode(decodedId, "utf8");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  protected String getDecodedId() {
    return decodedId;
  }

  @GET
  public ConnectorEntityRepresentation get() {
    try {
      return getRepresentation();
    } catch (URISyntaxException e) {
      LOGGER.error("Error creating entity representation " + getEntity() + ": " + e.getMessage(), e);
    }
    return null;
  }


  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public boolean delete() {
    ConnectorEntity entity = getEntity();
    return entity.delete();
  }


  protected void fillRepresentation(ConnectorEntity entity, ConnectorEntityRepresentation representation) {
    representation.setDeleteable(entity.isDeleteable());

    //fill dam data
    ConnectorModel model = new ConnectorModel(getContext(entity.getConnectorId()).getType(), getPreferredSite(), getLocale(), Collections.emptyList());
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

  protected Locale getLocale() {
    ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = sra.getRequest();
    return (Locale) req.getSession(false).getAttribute(SESSION_ATTRIBUTE_LOCALE);
  }

  protected Site getPreferredSite() {
    ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = sra.getRequest();
    return (Site) req.getSession(false).getAttribute(SESSION_ATTRIBUTE_PREFERRED_SITE);
  }

  protected abstract ConnectorEntityRepresentation getRepresentation() throws URISyntaxException;

  protected ConnectorContext getContext(ConnectorId id) {
    ConnectorContext context = connectorContextProvider.createContext(id.getConnectionId());
    ((ConnectorContextImpl)context).setLocale(getLocale());
    ((ConnectorContextImpl)context).setPreferredSite(getPreferredSite());
    return context;
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
}
