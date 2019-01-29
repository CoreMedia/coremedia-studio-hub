package com.coremedia.blueprint.connectors.filesystems;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableCaching
public class ConnectorFilesystemsConfiguration {
  //TODO remove whole package

  @Bean
  @Primary
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheCacheManager().getObject());
  }

  @Bean
  public EhCacheManagerFactoryBean ehCacheCacheManager() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }

}
