package com.coremedia.blueprint.connectors.filesystem;

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
public class ConnectorFileSystemConfiguration {

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorFileSystemService(@NonNull @Qualifier("fileSystemConnectorCache") FileSystemService fileSystemService) {
    FileSystemConnectorServiceImpl fileSystemConnectorService = new FileSystemConnectorServiceImpl();
    fileSystemConnectorService.setFileCache(fileSystemService);
    return fileSystemConnectorService;
  }

  @Bean (name="connector:filesystem")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection filesystemConnectorConnection(@NonNull @Qualifier("connectorFileSystemService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean(name = "fileSystemConnectorCache")
  public FileSystemService fileSystemConnectorCache() {
    return new FileSystemService();
  }
}
