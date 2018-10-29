package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.content.ConnectorContentConfiguration;
import com.coremedia.blueprint.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Configuration
@EnableCaching
@Import({CapRepositoriesConfiguration.class, ConnectorContentConfiguration.class})
public class ConnectorYoutubeConfiguration {
  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService connectorYouTubeService() {
    return new YouTubeConnectorServiceImpl();
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
  public CacheManager cacheManagerYouTube() {
    return new EhCacheCacheManager(youTubeCacheManagerFactory().getObject());
  }

  @Bean
  public YouTubeService youTubeService() {
    return new YouTubeService();
  }

  @Bean
  public EhCacheManagerFactoryBean youTubeCacheManagerFactory() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("youtube-ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }
}
