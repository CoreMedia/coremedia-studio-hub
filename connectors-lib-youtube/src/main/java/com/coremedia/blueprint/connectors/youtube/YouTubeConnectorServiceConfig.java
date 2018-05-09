package com.coremedia.blueprint.connectors.youtube;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableCaching
@ComponentScan({"com.coremedia.blueprint.connectors.youtube"})
public class YouTubeConnectorServiceConfig {

  @Bean
  public CacheManager cacheManagerYouTube() {
    return new EhCacheCacheManager(youTubeCacheManagerFactory().getObject());
  }

  @Bean
  public EhCacheManagerFactoryBean youTubeCacheManagerFactory() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("youtube-ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }
}