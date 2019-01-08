package com.coremedia.blueprint.connectors.s7;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

public class ConnectorS7Configuration {

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService connectorS7Service() {
    return new S7ConnectorServiceImpl();
  }

  @Bean(name = "connector:s7")
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorConnection s7ConnectorConnection(@NonNull @Qualifier("connectorS7Service") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public S7ContentItemWriteInterceptor s7ConnectorWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                          @NonNull ContentCreateService contentCreateService) {
    S7ContentItemWriteInterceptor s7ContentItemWriteInterceptor = new S7ContentItemWriteInterceptor();
    s7ContentItemWriteInterceptor.setContentCreateService(contentCreateService);
    s7ContentItemWriteInterceptor.setType(contentRepository.getContentType("CMPicture"));
    return s7ContentItemWriteInterceptor;
  }
}
