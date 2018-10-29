package com.coremedia.blueprint.connectors.caching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorCachingConfiguration {
  @Bean
  public TempFileCacheService tempFileCacheService(@Value("${connectors.preview.cacheSize:100}") int cacheSize) {
    TempFileCacheService tempFileCacheService = new TempFileCacheService();
    tempFileCacheService.setCacheSize(cacheSize);
    return tempFileCacheService;
  }
}
