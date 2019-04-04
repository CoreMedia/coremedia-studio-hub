package com.coremedia.blueprint.connectors.cloudinary;

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
public class ConnectorCloudinaryConfiguration {

  @Bean
  public CloudinaryService cloudinaryService() {
    return new CloudinaryService();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorCloudinaryService(@NonNull Cache cache) {
    return new CloudinaryConnectorServiceImpl(cache);
  }

  @Bean(name = "connector:cloudinary")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection cloudinaryConnectorConnection(@NonNull @Qualifier("connectorCloudinaryService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }
}
