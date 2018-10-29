package com.coremedia.blueprint.connectors.cae;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.connectors.cae.link.ConnectorLinkScheme;
import com.coremedia.blueprint.connectors.cae.web.taglib.ConnectorFreemarkerFacade;
import com.coremedia.blueprint.connectors.impl.ConnectorContextProvider;
import com.coremedia.blueprint.connectors.impl.ConnectorImplConfiguration;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.lang.NonNull;

@Configuration
@Import(ConnectorImplConfiguration.class)
@ImportResource(
        value = {
                "classpath:/com/coremedia/blueprint/base/settings/impl/bpbase-settings-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class
)
public class ConnectorsCaeConfiguration {

  @Bean
  public ConnectorFreemarkerFacade connectorFreemarkerFacade(@NonNull Connectors connector,
                                                             @NonNull ConnectorContextProvider connectorContextProvider,
                                                             @NonNull SettingsService settingsService) {
    ConnectorFreemarkerFacade connectorFreemarkerFacade = new ConnectorFreemarkerFacade();
    connectorFreemarkerFacade.setConnector(connector);
    connectorFreemarkerFacade.setContextProvider(connectorContextProvider);
    connectorFreemarkerFacade.setSettingsService(settingsService);
    return connectorFreemarkerFacade;
  }

  @Bean
  public ConnectorLinkScheme connectorLinkSchemes() {
    return new ConnectorLinkScheme();
  }
}
