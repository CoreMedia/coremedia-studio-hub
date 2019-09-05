package com.coremedia.blueprint.connectors.shutterstock;

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
public class ConnectorShutterstockConfiguration {
  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService connectorShutterstockService(@NonNull ShutterstockService shutterstockService) {
    return new ShutterstockConnectorServiceImpl(shutterstockService);
  }

  @Bean(name = "connector:shutterstock")
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorConnection shutterstockConnectorConnection(@NonNull @Qualifier("connectorShutterstockService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  public ShutterstockContentItemWriteInterceptor shutterstockContentItemWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                                         @NonNull ContentCreateService contentCreateService) {
    ShutterstockContentItemWriteInterceptor shutterstockContentItemWriteInterceptor = new ShutterstockContentItemWriteInterceptor();
    shutterstockContentItemWriteInterceptor.setPriority(0);
    shutterstockContentItemWriteInterceptor.setType(contentRepository.getContentType("CMPicture"));
    shutterstockContentItemWriteInterceptor.setContentCreateService(contentCreateService);
    return shutterstockContentItemWriteInterceptor;
  }

  @Bean
  public CacheManager cacheManagerShutterstock() {
    return new EhCacheCacheManager(shutterstockCacheManagerFactory().getObject());
  }

  @Bean
  public ShutterstockService shutterstockService() {
    return new ShutterstockService();
  }

  @Bean
  public EhCacheManagerFactoryBean shutterstockCacheManagerFactory() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("shutterstock-ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }
}
