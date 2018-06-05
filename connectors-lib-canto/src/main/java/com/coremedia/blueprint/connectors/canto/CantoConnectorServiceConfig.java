package com.coremedia.blueprint.connectors.canto;

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
@ComponentScan({"com.coremedia.blueprint.connectors.canto"})
public class CantoConnectorServiceConfig {

  @Bean
  public CacheManager cacheManagerCanto() {
    return new EhCacheCacheManager(cantoCacheManagerFactory().getObject());
  }

  @Bean
  public EhCacheManagerFactoryBean cantoCacheManagerFactory() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("canto-ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }
}