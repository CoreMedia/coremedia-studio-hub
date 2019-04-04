package com.coremedia.blueprint.connectors.coremedia;

import com.coremedia.connectors.api.ConnectorConnection;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.connectors.content.ConnectorContentConfiguration;
import com.coremedia.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
@Import({CapRepositoriesConfiguration.class, ConnectorContentConfiguration.class})
public class ConnectorCoreMediaConfiguration {

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorCoreMediaService() {
    return new CoreMediaConnectorServiceImpl();
  }

  @Bean(name = "connector:coremedia")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection coreMediaConnectorConnection(@NonNull @Qualifier("connectorCoreMediaService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public CoreMediaConnectorWriteInterceptor coreMediaConnectorWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                               @NonNull ContentCreateService contentCreateService) {
    CoreMediaConnectorWriteInterceptor coreMediaConnectorWriteInterceptor = new CoreMediaConnectorWriteInterceptor();
    coreMediaConnectorWriteInterceptor.setContentRepository(contentRepository);
    coreMediaConnectorWriteInterceptor.setContentCreateService(contentCreateService);
    coreMediaConnectorWriteInterceptor.setType(contentRepository.getContentType("CMTeasable"));
    return coreMediaConnectorWriteInterceptor;
  }
}
