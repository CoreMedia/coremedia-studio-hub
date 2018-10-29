package com.coremedia.blueprint.connectors.s3;

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
public class ConnectorS3Configuration {

  @Bean(name = "connector:s3")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection s3ConnectorConnection(@NonNull @Qualifier("connectorS3Service") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorS3Service(@NonNull @Qualifier("s3ConnectorCache") FileSystemService fileSystemService) {
    S3ConnectorServiceImpl s3ConnectorService = new S3ConnectorServiceImpl();
    s3ConnectorService.setFileCache(fileSystemService);
    return s3ConnectorService;
  }

  @Bean
  public FileSystemService s3ConnectorCache() {
    return new FileSystemService();
  }
}
