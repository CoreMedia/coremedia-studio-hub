package com.coremedia.blueprint.connectors.cloudinary;

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
@ComponentScan({"com.coremedia.blueprint.connectors.cloudinary"})
public class CloudinaryConnectorServiceConfig {

  @Bean
  public CacheManager cacheManagerCloudinary() {
    return new EhCacheCacheManager(cloudinaryCacheManagerFactory().getObject());
  }

  @Bean
  public EhCacheManagerFactoryBean cloudinaryCacheManagerFactory() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("cloudinary-ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }

}