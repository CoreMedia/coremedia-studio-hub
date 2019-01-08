package com.coremedia.blueprint.connectors.brightcove;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

public class ConnectorBrightcoveConfiguration {

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService connectorBrightcoveService() {
    return new BrightcoveConnectorService();
  }

  @Bean(name = "connector:brightcove")
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorConnection s7ConnectorConnection(@NonNull @Qualifier("connectorBrightcoveService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public BrightcoveWriteInterceptor brightcoveConnectorWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                        @NonNull ContentCreateService contentCreateService) {
    BrightcoveWriteInterceptor brightcoveWriteInterceptor = new BrightcoveWriteInterceptor();
    brightcoveWriteInterceptor.setContentCreateService(contentCreateService);
    brightcoveWriteInterceptor.setType(contentRepository.getContentType("CMVideo"));
    return brightcoveWriteInterceptor;
  }
}
