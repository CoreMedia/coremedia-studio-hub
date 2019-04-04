package com.coremedia.blueprint.connectors.s3;

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
  public ConnectorService connectorS3Service(@NonNull Cache cache) {
    return new S3ConnectorServiceImpl(cache);
  }
}
