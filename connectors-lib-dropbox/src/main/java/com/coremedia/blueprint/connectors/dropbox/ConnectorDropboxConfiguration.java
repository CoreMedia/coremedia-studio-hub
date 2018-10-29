package com.coremedia.blueprint.connectors.dropbox;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.filesystems.FileSystemService;
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
  public ConnectorService connectorDropboxService(@NonNull @Qualifier("dropboxFileCache") FileSystemService fileSystemService) {
    DropboxConnectorServiceImpl dropboxConnectorService = new DropboxConnectorServiceImpl();
    dropboxConnectorService.setFileCache(fileSystemService);
    return dropboxConnectorService;
  }

  @Bean(name = "connector:dropbox")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection dropboxConnectorConnection(@NonNull @Qualifier("connectorDropboxService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public FileSystemService dropboxFileCache() {
    return new FileSystemService();
  }
}
