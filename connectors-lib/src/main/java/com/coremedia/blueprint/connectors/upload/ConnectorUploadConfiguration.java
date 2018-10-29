package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.impl.ConnectorImplConfiguration;
import com.coremedia.blueprint.connectors.impl.Connectors;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ConnectorImplConfiguration.class)
public class ConnectorUploadConfiguration {
  @Bean
  public ConnectorContentUploadService connectorContentUploadService(@NonNull Connectors connectors) {
    ConnectorContentUploadService connectorContentUploadService = new ConnectorContentUploadService();
    connectorContentUploadService.setConnectors(connectors);
    return connectorContentUploadService;
  }
}
