package com.coremedia.blueprint.connectors.typeform;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ConnectorTypeformConfiguration {

  @Bean(name = "connector:typeform")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection s3ConnectorConnection(@NonNull @Qualifier("connectorTypeformService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorTypeformService() {
    return new TypeformConnectorService();
  }


  @Bean
  public TypeformItemWriteInterceptor typeformContentItemWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                     @NonNull ContentCreateService contentCreateService) {
    TypeformItemWriteInterceptor writeInterceptor = new TypeformItemWriteInterceptor();
    writeInterceptor.setPriority(0);
    writeInterceptor.setType(contentRepository.getContentType("CMHTML"));
    writeInterceptor.setContentCreateService(contentCreateService);
    return writeInterceptor;
  }
}
