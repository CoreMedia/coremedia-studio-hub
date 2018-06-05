package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The class where all connections are stored.
 */
public class Connectors implements BeanFactoryAware, BeanNameAware, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(Connectors.class);

  private BeanFactory beanFactory;
  private String beanName;
  private Map<String, ConnectorConnection> connections = new HashMap<>();
  private ConnectorContextProvider connectorContextProvider;
  private SitesService sitesService;
  private List<ConnectorConnectionListener> connectionListeners = new ArrayList<>();
  private List<ConnectorCategoryChangeListener> connectorCategoryChangeListeners = new ArrayList<>();


  @Nullable
  public ConnectorConnection getConnection(ConnectorContext context) {
    String connectionId = context.getConnectionId();
    if(connectionId.startsWith(ConnectorConnection.CONNECTOR_PREFIX)) {
      connectionId = connectionId.split(":")[1];
    }

    if(!connections.containsKey(connectionId)) {
      storeConnection(context);
    }

    return connections.get(connectionId);
  }

  @Nonnull
  public List<ConnectorType> getConnectorTypes() {
    List<ConnectorType> result = new ArrayList<>();
    List<ConnectorContext> contexts = connectorContextProvider.getContexts();
    contexts.sort(Comparator.comparing(ConnectorContext::getTypeName));

    for (ConnectorContext context : contexts) {
      String typeName = context.getType();
      ConnectorType cType = new ConnectorType(typeName);
      if(!result.contains(cType)) {
        cType.setRootNodeVisible(context.isRootNodeVisible());
        result.add(cType);
      }
    }
    return result;
  }

  @Nonnull
  public List<ConnectorConnection> getConnectionsByType(String siteId, Locale locale, String type) {
    if(siteId == null) {
      return Collections.emptyList();
    }

    List<ConnectorContext> filtered = new ArrayList<>();
    Site site = sitesService.getSite(siteId);
    List<ConnectorContext> contexts = connectorContextProvider.findContexts(site);
    for (ConnectorContext context : contexts) {
      if(context.getType().equals(type)) {
        filtered.add(context);
      }
    }

    //apply current locale to the actual used connections
    List<ConnectorConnection> connections = getConnections(filtered);
    for (ConnectorConnection connection : connections) {
      ((ConnectorContextImpl)connection.getContext()).setLocale(locale);
      ((ConnectorContextImpl)connection.getContext()).setPreferredSite(sitesService.getSite(siteId));
    }
    return connections;
  }

  public boolean isValid(ConnectorConnection connection) {
    return this.connections.containsValue(connection);
  }

  public void addConnectionListener(ConnectorConnectionListener listener) {
    this.connectionListeners.add(listener);
  }

  public void addConnectorCategoryChangeListener(ConnectorCategoryChangeListener listener) {
    this.connectorCategoryChangeListeners.add(listener);
  }

  public void notifyCategoryChange(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category) {
    for (ConnectorCategoryChangeListener connectorCategoryChangeListener : connectorCategoryChangeListeners) {
      connectorCategoryChangeListener.categoryChanged(context, category);
    }
  }

  //------------- Helper -----------------------------------------------------------------------------------------------

  private List<ConnectorConnection> getConnections(List<ConnectorContext> contexts) {
    try {
      List<ConnectorConnection> result = new ArrayList<>();
      for (ConnectorContext context : contexts) {
        String connectionId = context.getConnectionId();
        if(connections.containsKey(connectionId)) {
          //the dirty flag is set when the object has been created
          if(((ConnectorContextImpl)context).isDirty()) {
            //so we know that we have to recreate the connection
            ConnectorConnection connectorConnection = connections.get(connectionId);
            connections.remove(context.getConnectionId());
            connectorConnection.getConnectorService().shutdown(connectorConnection.getContext());
            connectionListeners.forEach(l -> l.terminated(connectorConnection));

            //recreate connection
            storeConnection(context);
          }
        }
        else {
          storeConnection(context);
        }
      }

      //finally collect the valid connections
      for (ConnectorContext context : contexts) {
        if(context.isEnabled()) {
          ConnectorConnection connectorConnection = connections.get(context.getConnectionId());
          //connection may not be available when the init() method has failed
          if(connectorConnection != null) {
            result.add(connectorConnection);
          }
        }
      }

      return result;
    } catch (Exception e) {
      LOG.error("Could not retrieve connector bean for connections {}.", e);
      return null;
    }
  }

  private void storeConnection(ConnectorContext context) {
    String beanDefinitionId = this.beanName + ":" + context.getType();
    try {
      ConnectorConnection connection = (ConnectorConnection) beanFactory.getBean(beanDefinitionId);
      if (context.isEnabled()) {
        ((ConnectorContextImpl)context).setDirty(false);
        connection.setConnectorContext(context);
        boolean init = connection.getConnectorService().init(context);
        if(init) {
          connections.put(context.getConnectionId(), connection);
          connectionListeners.forEach(l -> l.initialized(connection));
        }
      }
    } catch (Exception e) {
      LOG.warn("Could not retrieve connector bean for connection bean id '{}': " + e.getMessage(), beanDefinitionId);
    }
  }

  //-------------------------------- Spring ----------------------------------------------------------------------------

  public void setBeanFactory(@Nonnull BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  public void setBeanName(@Nonnull String beanName) {
    this.beanName = beanName;
  }

  public void afterPropertiesSet() throws Exception {
    if (this.beanFactory == null) {
      throw new IllegalStateException("BeanFactory must be set.");
    } else if (this.beanName == null) {
      throw new IllegalStateException("BeanName must be set.");
    }
  }

  @Required
  public void setConnectorContextProvider(ConnectorContextProvider connectorContextProvider) {
    this.connectorContextProvider = connectorContextProvider;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }
}
