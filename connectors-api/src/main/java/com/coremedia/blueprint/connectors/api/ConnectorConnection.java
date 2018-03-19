package com.coremedia.blueprint.connectors.api;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;

/**
 * Represents the actual connection to an external system.
 * Each connection has a context and the corresponding service it has been initialized with.
 * Usually there is only one default implementation of the connection since the actual logic
 * is implemented in the ConnectorService instances.
 *
 * The class implemented the BeanNameAware interface since instances of this class
 * are created for each connection definition in the Connections.xml.
 */
public class ConnectorConnection implements BeanNameAware {
  public static final String CONNECTOR_PREFIX = "connector";

  private ConnectorService connectorService;
  private ConnectorContentService connectorContentService;
  private String connectorType;
  private ConnectorContext context;

  @Nonnull
  public ConnectorService getConnectorService() {
    return connectorService;
  }

  @Nonnull
  public ConnectorContext getContext() {
    return context;
  }

  @Required
  public void setConnectorService(ConnectorService connectorService) {
    this.connectorService = connectorService;
  }

  @Required
  public void setConnectorContentService(ConnectorContentService connectorContentService) {
    this.connectorContentService = connectorContentService;
  }

  @Nonnull
  public ConnectorContentService getConnectorContentService() {
    return connectorContentService;
  }

  @Nonnull
  public String getConnectorType() {
    return connectorType;
  }

  public void setConnectorContext(ConnectorContext context) {
    this.context = context;
  }

  @Override
  public void setBeanName(@Nonnull String beanName) {
    if(!beanName.startsWith(CONNECTOR_PREFIX + ":")) {
      throw new UnsupportedOperationException("ConnectorConnection bean name must have a leading " + CONNECTOR_PREFIX + ": prefix");
    }
    String[] split = beanName.split(":");
    this.connectorType = split[1];
  }

  @Override
  public String toString() {
    return "Connector Connection '" + connectorType + "'";
  }
}
