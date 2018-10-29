package com.coremedia.blueprint.connectors.previewconverters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ConnectorPreviewConvertersConfiguration {

  @Bean
  public ConnectorPreviewConverter connectorMdPreviewConverter() {
    return new CommonMarkConverter();
  }

  @Bean
  public ConnectorPreviewConverter connectorTextPreviewConverter() {
    return new TextConverter();
  }

  @Bean
  public ConnectorPreviewConverter connectorOfficePreviewConverter() {
    return new OfficeConverter();
  }

  @Bean
  public List<ConnectorPreviewConverter> connectorPreviewConverters() {
    ArrayList<ConnectorPreviewConverter> connectorPreviewConverters = new ArrayList<>();
    connectorPreviewConverters.add(connectorMdPreviewConverter());
    connectorPreviewConverters.add(connectorTextPreviewConverter());
    connectorPreviewConverters.add(connectorOfficePreviewConverter());
    return connectorPreviewConverters;
  }
}
