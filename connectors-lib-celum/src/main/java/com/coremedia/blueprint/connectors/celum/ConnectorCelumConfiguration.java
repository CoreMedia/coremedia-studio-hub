package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.content.ConnectorContentConfiguration;
import com.coremedia.blueprint.connectors.content.ContentCreateService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableCaching
@Import({
                CapRepositoriesConfiguration.class,
                ConnectorContentConfiguration.class
        })
public class ConnectorCelumConfiguration {

  @Bean(name="connector:celum")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection celumConnectorConnection(@NonNull @Qualifier("connectorCelumService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }
  @Bean
  public CacheManager cacheManagerCelum() {
    return new EhCacheCacheManager(celumCacheManagerFactory().getObject());
  }

  @Bean
  public EhCacheManagerFactoryBean celumCacheManagerFactory() {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource("celum-ehcache.xml"));
    cmfb.setShared(false);
    return cmfb;
  }

  @Bean
  public CelumContentItemWriteInterceptor celumContentItemWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                                           @NonNull ContentCreateService contentCreateService) {
    CelumContentItemWriteInterceptor celumContentItemWriteInterceptor = new CelumContentItemWriteInterceptor();
    celumContentItemWriteInterceptor.setType(contentRepository.getContentType("CMPicture"));
    celumContentItemWriteInterceptor.setContentCreateService(contentCreateService);
    celumContentItemWriteInterceptor.setPriority(0);
    return celumContentItemWriteInterceptor;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public CelumCoraService celumCoraService() {
    return new CelumCoraService();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public CelumConnectorService connectorCelumService(@NonNull CelumCoraService celumCoraService) {
    return new CelumConnectorService(celumCoraService);
  }
}
