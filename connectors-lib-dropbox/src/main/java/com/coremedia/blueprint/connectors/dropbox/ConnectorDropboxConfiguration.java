package com.coremedia.blueprint.connectors.dropbox;

import com.coremedia.connectors.api.ConnectorConnection;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.cache.Cache;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ConnectorDropboxConfiguration {

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorDropboxService(@NonNull Cache cache) {
    return new DropboxConnectorServiceImpl(cache);
  }

  @Bean(name = "connector:dropbox")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection dropboxConnectorConnection(@NonNull @Qualifier("connectorDropboxService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }
}
