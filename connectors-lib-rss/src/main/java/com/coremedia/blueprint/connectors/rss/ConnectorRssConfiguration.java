package com.coremedia.blueprint.connectors.rss;

import com.coremedia.connectors.api.ConnectorConnection;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.connectors.content.ConnectorContentConfiguration;
import com.coremedia.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Configuration
@Import({CapRepositoriesConfiguration.class, ConnectorContentConfiguration.class})
public class ConnectorRssConfiguration {
  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService connectorRssService() {
    return new RssConnectorServiceImpl();
  }

  @Bean(name = "connector:rss")
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorConnection rssConnectorConnection(@NonNull @Qualifier("connectorRssService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public RssContentItemWriteInterceptor rssContentItemWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                       @NonNull ContentCreateService contentCreateService) {
    RssContentItemWriteInterceptor rssContentItemWriteInterceptor = new RssContentItemWriteInterceptor();
    rssContentItemWriteInterceptor.setPriority(0);
    rssContentItemWriteInterceptor.setType(contentRepository.getContentType("CMArticle"));
    rssContentItemWriteInterceptor.setContentCreateService(contentCreateService);
    return rssContentItemWriteInterceptor;
  }
}
