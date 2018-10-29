package com.coremedia.blueprint.connectors.cloudinary;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Configuration
@EnableCaching
public class ConnectorCloudinaryConfiguration {

  @Bean
  public CloudinaryService cloudinaryService() {
    return new CloudinaryService();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorCloudinaryService() {
    return new CloudinaryConnectorServiceImpl();
  }

  @Bean(name = "connector:cloudinary")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection cloudinaryConnectorConnection(@NonNull @Qualifier("connectorCloudinaryService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

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