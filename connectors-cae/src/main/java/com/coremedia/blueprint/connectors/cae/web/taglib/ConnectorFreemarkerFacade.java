package com.coremedia.blueprint.connectors.cae.web.taglib;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.common.contentbeans.CMMedia;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames;
import com.coremedia.blueprint.connectors.impl.Connectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


public class ConnectorFreemarkerFacade {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorFreemarkerFacade.class);

  private SettingsService settingsService;
  private Connectors connector;
  private ConnectorContextProvider contextProvider;

  public ConnectorItem getConnectorItem(CMMedia source) {
    String idString = settingsService.setting(ConnectorPropertyNames.CONNECTOR_ID, String.class, source);
    ConnectorId connectorId = ConnectorId.toId(idString);
    ConnectorContext context = contextProvider.createContext(connectorId.getConnectionId());
    ConnectorConnection connection = connector.getConnection(context);

    if (connection != null) {
      ConnectorService connectorService = connection.getConnectorService();
      ConnectorItem item = connectorService.getItem(context, connectorId);
      if(item == null) {
        LOG.warn("Failed to retrieve connector item for connectorId '" + idString + "'");
      }
    }
    return null;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setConnector(Connectors connector) {
    this.connector = connector;
  }

  @Required
  public void setContextProvider(ConnectorContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }
}
