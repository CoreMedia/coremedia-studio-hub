package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(CapRepositoriesConfiguration.class)
@ImportResource(
        value = {
                "classpath:/com/coremedia/cap/multisite/multisite-services.xml",
                "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class
)
public class ConnectorImplConfiguration {
  @Bean
  public Connectors connector(@NonNull SitesService sitesService,
                              @NonNull ConnectorContextProvider connectorContextProvider) {
    Connectors connectors = new Connectors();
    connectors.setSitesService(sitesService);
    connectors.setConnectorContextProvider(connectorContextProvider);
    return connectors;
  }

  @Bean
  public ConnectorContextProvider connectorContextProvider(@NonNull Cache cache,
                                                           @NonNull ContentRepository contentRepository,
                                                           @NonNull SitesService sitesService,
                                                           @NonNull SettingsService settingsService,
                                                           @Value("${connectors.configpath.global:/Settings/Options/Settings/Connectors}") String globalConfigPath,
                                                           @Value("${connectors.configpath.site:/Options/Settings}") String siteConfigPath) {
    ConnectorContextProvider connectorContextProvider = new ConnectorContextProvider();
    connectorContextProvider.setCache(cache);
    connectorContextProvider.setContentRepository(contentRepository);
    connectorContextProvider.setSitesService(sitesService);
    connectorContextProvider.setSettingsService(settingsService);
    connectorContextProvider.setGlobalConfigPath(globalConfigPath);
    connectorContextProvider.setSiteConfigPath(siteConfigPath);
    return connectorContextProvider;
  }
}
