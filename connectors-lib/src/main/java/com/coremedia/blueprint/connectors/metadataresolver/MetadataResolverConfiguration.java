package com.coremedia.blueprint.connectors.metadataresolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MetadataResolverConfiguration {

  @Bean
  public ConnectorMetaDataResolver connectorAudioMetaDataResolver() {
    return new AudioMetaDataResolver();
  }

  @Bean
  public ConnectorMetaDataResolver connectorPictureMetaDataResolver() {
    return new PictureMetaDataResolver();
  }

  @Bean
  public ConnectorMetaDataResolver connectorPdfMetaDataResolver() {
    return new PdfMetaDataResolver();
  }

  @Bean
  public List<ConnectorMetaDataResolver> connectorMetaDataResolvers() {
    ArrayList<ConnectorMetaDataResolver> connectorMetaDataResolvers = new ArrayList<>();
    connectorMetaDataResolvers.add(connectorAudioMetaDataResolver());
    connectorMetaDataResolvers.add(connectorPictureMetaDataResolver());
    connectorMetaDataResolvers.add(connectorPdfMetaDataResolver());
    return connectorMetaDataResolvers;
  }
}
