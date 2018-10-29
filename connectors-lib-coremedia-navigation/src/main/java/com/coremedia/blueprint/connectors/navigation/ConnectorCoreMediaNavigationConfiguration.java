package com.coremedia.blueprint.connectors.navigation;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.navigation.util.ConnectorPageGridService;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration
@Import({CapRepositoriesConfiguration.class})
@ImportResource(
        value = {
                "classpath:/com/coremedia/cap/multisite/multisite-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class
)
public class ConnectorCoreMediaNavigationConfiguration {

  @Bean
  public ConnectorPageGridService connectorPageGridService(@NonNull SitesService sitesService) {
    ConnectorPageGridService connectorPageGridService = new ConnectorPageGridService();
    connectorPageGridService.setPlacementPaths("Options/Settings/Pagegrid/Placements");
    connectorPageGridService.setSitesService(sitesService);
    return connectorPageGridService;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorNavigationService(@NonNull ContentRepository contentRepository,
                                                     @NonNull SitesService sitesService,
                                                     @NonNull ConnectorPageGridService connectorPageGridService) {
    NavigationConnectorServiceImpl navigationConnectorService = new NavigationConnectorServiceImpl();
    navigationConnectorService.setContentRepository(contentRepository);
    navigationConnectorService.setSitesService(sitesService);
    navigationConnectorService.setPageGridService(connectorPageGridService);
    return navigationConnectorService;
  }

  @Bean(name = "connector:coremedia-navigation")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection coreMediaNavigationConnectorConnection(@NonNull @Qualifier("connectorNavigationService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }
}
