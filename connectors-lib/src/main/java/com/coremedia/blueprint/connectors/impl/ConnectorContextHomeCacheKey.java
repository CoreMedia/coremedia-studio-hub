package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.struct.Struct;

import java.util.ArrayList;
import java.util.List;

/**
 * Caching of the Connections.xml
 */
public class ConnectorContextHomeCacheKey extends ConnectorContextCacheKey {
  private String homePath;

  ConnectorContextHomeCacheKey(ContentRepository contentRepository,
                               SettingsService settingsService,
                               String globalConfigPath) {
    super(contentRepository, settingsService, null, null, globalConfigPath);
    this.homePath = contentRepository.getConnection().getSession().getUser().getHomeFolder().getPath();
  }

  @Override
  public List<ConnectorContext> evaluate(Cache cache) {
    List<ConnectorContext> result = new ArrayList<>();

      //check home sweet home
      Content homeFolder = contentRepository.getChild(homePath);
      List homeSettings = findConnectorSettings(homeFolder.getPath() + "/" + CONNECTIONS_DOCUMENT, CONNECTIONS_STRUCT);
      for (Object setting : homeSettings) {
        if (setting instanceof Struct) {
          result.add(new ConnectorContextImpl(((Struct) setting).getProperties()));
        }
      }

    return updateContextsWithConnectorType(result);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ConnectorContextHomeCacheKey)) {
      return false;
    }

    ConnectorContextHomeCacheKey that = (ConnectorContextHomeCacheKey) o;
    return homePath.equals(that.homePath);
  }

  @Override
  public int hashCode() {
    return homePath.hashCode();
  }
}
