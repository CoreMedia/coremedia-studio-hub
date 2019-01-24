package com.coremedia.blueprint.connectors.sfmc;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.sfmc.rest.SFMCConnector;
import com.coremedia.blueprint.connectors.sfmc.rest.SFMCService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ConnectorSFMCConfiguration {

  @Bean(name = "connector:sfmc")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection sfmcConnectorConnection(@NonNull @Qualifier("connectorSFMCService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorSFMCService(@NonNull @Qualifier("sfmcService") SFMCService sfmcService) {
    SFMCConnectorServiceImpl sfmcConnectorService = new SFMCConnectorServiceImpl();
    sfmcConnectorService.setSfmcService(sfmcService);
    return sfmcConnectorService;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public SFMCConnector sfmcConnector() {
    return new SFMCConnector();
  }


  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public SFMCService sfmcService(@NonNull SFMCConnector sfmcConnector) {
    return new SFMCService(sfmcConnector);
  }
}
