package com.coremedia.blueprint.studio.connectors.rest.invalidation;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.impl.ConnectorConnectionListener;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.studio.connectors.rest.notifications.ConnectorNotificationService;
import com.coremedia.rest.invalidations.SimpleInvalidationSource;
import com.coremedia.rest.linking.Linker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for scheduling connector invalidations.
 */
public class ConnectorInvalidator extends SimpleInvalidationSource implements ConnectorConnectionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorInvalidator.class);

  private Connectors connectors;
  private Linker linker;

  private List<ServiceInvalidationThread> tasks = new ArrayList<>();
  private ConnectorNotificationService connectorNotificationService;

  public ConnectorInvalidator(String id, Connectors connector, Linker linker, ConnectorNotificationService connectorNotificationService) {
    super();

    this.setId(id);
    this.connectors = connector;
    this.linker = linker;
    this.connectorNotificationService = connectorNotificationService;
  }

  @Override
  public void initialized(ConnectorConnection connection) {
    long invalidationInterval = connection.getContext().getInvalidationInterval();
    if (invalidationInterval > 0) {
      schedule(connection);
    }
    else {
      LOGGER.info("Skipped invalidation scheduling for " + connection + " since no interval was configured.");
    }
  }

  @Override
  public void terminated(ConnectorConnection connection) {
    Optional<ServiceInvalidationThread> task = tasks.stream().filter(t -> t.connection.equals(connection)).findFirst();
    if (task.isPresent()) {
      ServiceInvalidationThread invalidationThread = task.get();
      invalidationThread.cancel();
      tasks.remove(invalidationThread);
    }
  }

  @Override
  public void shutdown() {
    for (ServiceInvalidationThread task : tasks) {
      task.cancel();
    }
  }

  //---------------------- Spring --------------------------------------------------------------------------------------
  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    connectors.addConnectionListener(this);
  }

  @Required
  public void setConnectors(Connectors connectors) {
    this.connectors = connectors;
  }

  @Required
  public void setLinker(Linker linker) {
    this.linker = linker;
  }

  @Required
  public void setConnectorNotificationService(ConnectorNotificationService connectorNotificationService) {
    this.connectorNotificationService = connectorNotificationService;
  }

  /**
   * Helper for setting up the scheduled task
   */
  private void schedule(ConnectorConnection connection) {
    ServiceInvalidationThread callable = new ServiceInvalidationThread(connection);
    callable.start();
    LOGGER.info("Scheduled " + callable);
  }

  /**
   * The runnable that is called to invalidate a connector service.
   */
  class ServiceInvalidationThread extends Thread {
    private ConnectorConnection connection;
    private ConnectorService connectorService;
    private boolean valid = true;

    ServiceInvalidationThread(ConnectorConnection connection) {
      super("Invalidation thread for '" + connection.getContext().getConnectionId() + "'");
      this.connection = connection;
      this.connectorService = connection.getConnectorService();
    }

    public void run() {
      long interval = connection.getContext().getInvalidationInterval();
      //check if the connection is still valid
      while (connectors.isValid(connection) && valid) {
        //so we are waiting for the next call
        try {
          long start = System.currentTimeMillis();
          LOGGER.debug("Invalidating connector connection '" + connection.getContext().getConnectionId() + "'");
          InvalidationResult invalidationResult = connectorService.invalidate(connection.getContext());

          //build links to the RemoteBeans to invalidationResult
          if (invalidationResult != null) {
            Set<String> links = new HashSet<>();
            List<ConnectorEntity> entities = invalidationResult.getEntities();
            for (ConnectorEntity entity : entities) {
              URI link = linker.link(entity);
              links.add(link.toString());
            }
            addInvalidations(links);
            connectorNotificationService.sendInvalidationNotification(invalidationResult);

            long end = System.currentTimeMillis();
            LOGGER.debug(this.getName() + " finished, took " + (end - start) + " ms., next invalidation in " + interval + " seconds.");
          }
        } catch (Exception e) {
          LOGGER.error("Error invalidating " + getName() + ": " + e.getMessage(), e);
        } finally {
          try {
            TimeUnit.SECONDS.sleep(interval);
          } catch (InterruptedException e) {
            break;
          }
        }
      }

      LOGGER.info(this + " has not been re-scheduled since the connection is no longer valid.");
    }

    void cancel() {
      valid = false;
    }

    @Override
    public String toString() {
      return "Invalidation thread for " + connection.getContext().getConnectionId();
    }
  }
}
