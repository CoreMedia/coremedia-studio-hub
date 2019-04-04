package com.coremedia.blueprint.connectors.filesystem;

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
public class ConnectorFileSystemConfiguration {

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorFileSystemService(@NonNull Cache cache) {
    return new FileSystemConnectorServiceImpl(cache);
  }

  @Bean (name="connector:filesystem")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection filesystemConnectorConnection(@NonNull @Qualifier("connectorFileSystemService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }
}
