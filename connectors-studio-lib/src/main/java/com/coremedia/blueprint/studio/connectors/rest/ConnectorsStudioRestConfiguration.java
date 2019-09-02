package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.caching.TempFileCacheService;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.blueprint.connectors.metadataresolver.ConnectorMetaDataResolver;
import com.coremedia.blueprint.connectors.previewconverters.ConnectorPreviewConverter;
import com.coremedia.blueprint.connectors.upload.ConnectorContentUploadService;
import com.coremedia.blueprint.studio.connectors.rest.content.ConnectorContentService;
import com.coremedia.blueprint.studio.connectors.rest.invalidation.ConnectorCategoryInvalidator;
import com.coremedia.blueprint.studio.connectors.rest.invalidation.ConnectorInvalidator;
import com.coremedia.blueprint.studio.connectors.rest.notifications.ConnectorNotificationService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.rest.cap.content.search.solr.SolrSearchService;
import com.coremedia.rest.linking.Linker;
import com.coremedia.springframework.customizer.Customize;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;

import java.util.List;

@Configuration
@Import({
        com.coremedia.blueprint.connectors.ConnectorBundleConfiguration.class
})
@ImportResource(value = {
        "classpath:/com/coremedia/cap/common/uapi-services.xml",
        "classpath:/com/coremedia/cap/multisite/multisite-services.xml", // for "sitesService"
        "classpath:META-INF/coremedia/component-connectors-lib.xml"
}, reader = ResourceAwareXmlBeanDefinitionReader.class)
public class ConnectorsStudioRestConfiguration {

  @Bean
  @Scope("prototype")
  ConnectorServiceResource connectorServiceResource(Connectors connectors, ConnectorContextProvider connectorContextProvider) {
    return new ConnectorServiceResource(connectors, connectorContextProvider);
  }

  @Bean
  @Scope("prototype")
  ConnectorContentServiceResource connectorContentServiceResource(Connectors connectors, ConnectorContextProvider connectorContextProvider, SitesService sitesService, ContentRepository contentRepository, ConnectorContentService connectorContentService) {
    return new ConnectorContentServiceResource(connectors, connectorContextProvider, sitesService, contentRepository, connectorContentService);
  }

  @Bean
  ConnectorInvalidator connectorInvalidator(Connectors connectors, Linker linker, ConnectorNotificationService connectorNotificationService, @Value("${studio.rest.eventsCache.capacity:10000}") int capacity) {
    ConnectorInvalidator connectorInvalidator = new ConnectorInvalidator("connectorInvalidator", connectors, linker, connectorNotificationService);
    connectorInvalidator.setCapacity(capacity);
    return connectorInvalidator;
  }

  @Bean
  ConnectorCategoryInvalidator connectorCategoryInvalidator(Connectors connectors, Linker linker, @Value("${studio.rest.eventsCache.capacity:10000}") int capacity) {
    ConnectorCategoryInvalidator connectorCategoryInvalidator = new ConnectorCategoryInvalidator("connectorCategoryInvalidator", connectors, linker);
    connectorCategoryInvalidator.setCapacity(capacity);
    return connectorCategoryInvalidator;
  }

  @Bean
  @Scope("prototype")
  ConnectorContentService connectorContentService(Connectors connectors, ConnectorContextProvider connectorContextProvider, ContentRepository contentRepository, SolrSearchService solrSearchService) {
    return new ConnectorContentService(connectors, connectorContextProvider, contentRepository, solrSearchService);
  }

  @Bean
  ConnectorNotificationService connectorNotificationService(ContentRepository contentRepository) {
    return new ConnectorNotificationService(contentRepository);
  }

  @Bean
  @Scope("prototype")
  ConnectorsResource connectorsResource(Connectors connectors) {
    return new ConnectorsResource(connectors);
  }

  @Bean
  @Scope("prototype")
  ConnectorResource connectorResource(Connectors connectors) {
    return new ConnectorResource(connectors);
  }

  @Bean
  @Scope("prototype")
  ConnectorItemResource connectorItemResource(List<ConnectorPreviewConverter> connectorPreviewConverters, List<ConnectorMetaDataResolver> connectorMetaDataResolvers, MimeTypeService mimeTypeService, ContentRepository contentRepository, TempFileCacheService tempFileCacheService) {
    return new ConnectorItemResource(connectorPreviewConverters, connectorMetaDataResolvers, mimeTypeService, contentRepository, tempFileCacheService);
  }

  @Bean
  @Scope("prototype")
  ConnectorCategoryResource connectorCategoryResource(ConnectorContentUploadService connectorUploadService, ContentRepository contentRepository, TempFileCacheService tempFileCacheService) {
    return new ConnectorCategoryResource(connectorUploadService, contentRepository, tempFileCacheService);
  }

  @Bean
  @Customize(value = "customConverters", mode = Customize.Mode.APPEND)
  public List<HttpMessageConverter<?>> appendCustomConverters() {
    return List.of(new ResourceHttpMessageConverter());
  }



}
