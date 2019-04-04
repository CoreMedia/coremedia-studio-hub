package com.coremedia.blueprint.connectors.canto;

import com.coremedia.connectors.api.ConnectorConnection;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.canto.rest.services.AssetService;
import com.coremedia.blueprint.connectors.canto.rest.services.MetadataService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableCaching
public class ConnectorCantoConfiguration {

  @Bean
  public MetadataService cantoMetaDataService() {
    return new MetadataService();
  }

  @Bean
  public AssetService cantoAssetService() {
    return new AssetService();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorCantoService() {
    return new CantoConnectorServiceImpl();
  }

  @Bean(name = "connector:canto")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection cantoConnector(@NonNull @Qualifier("connectorCantoService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

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
