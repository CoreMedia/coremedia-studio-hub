package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.connectors.api.ConnectorConnection;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.connectors.content.ConnectorContentConfiguration;
import com.coremedia.connectors.content.ContentCreateService;
import com.coremedia.cache.Cache;
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
public class ConnectorYoutubeConfiguration {
  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService connectorYouTubeService(@NonNull Cache cache) {
    return new YouTubeConnectorServiceImpl(cache);
  }

  @Bean(name = "connector:youtube")
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorConnection youtubeConnectorConnection(@NonNull @Qualifier("connectorYouTubeService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public YouTubeContentItemWriteInterceptor youTubeContentItemWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                               @NonNull ContentCreateService contentCreateService) {
    YouTubeContentItemWriteInterceptor youTubeContentItemWriteInterceptor = new YouTubeContentItemWriteInterceptor();
    youTubeContentItemWriteInterceptor.setPriority(0);
    youTubeContentItemWriteInterceptor.setType(contentRepository.getContentType("CMVideo"));
    youTubeContentItemWriteInterceptor.setContentCreateService(contentCreateService);
    return youTubeContentItemWriteInterceptor;
  }

  @Bean
  public YouTubeService youTubeService() {
    return new YouTubeService();
  }
}
