package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cache.Cache;
import com.coremedia.cap.common.CapSession;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

/**
 * This context provider is ued to read all global and or site specific connector configurations.
 * It used a cache key to store the settings.
 */
public class ConnectorContextProvider {
  private final static String DEFAULT_GLOBAL_CONFIGURATION_PATH = "/Settings/Options/Settings";
  private final static String DEFAULT_SITE_CONFIGURATION_PATH = "/Options/Settings";

  private ContentRepository contentRepository;
  private SettingsService settingsService;
  private SitesService sitesService;
  private Cache cache;

  private String globalConfigPath, siteConfigPath;

  public ConnectorContext createContext(@NonNull String connectionId) {
    for (ConnectorContext context : getContexts()) {
      if(context.getConnectionId().equals(connectionId)) {
        return context;
      }
    }
    return null;
  }

  @NonNull
  public List<ConnectorContext> findContexts(@Nullable String siteId) {
    List<ConnectorContext> result = new ArrayList<>();
    for (ConnectorContext context : getContexts()) {
      String contextSiteId = ((ConnectorContextImpl)context).getSiteId();
      //global or home
      if(contextSiteId == null) {
        result.add(context);
      }
      else if(siteId != null && siteId.equals(contextSiteId)) { //site specific
        result.add(context);
      }
    }
    return result;
  }

  public List<ConnectorContext> getContexts() {
    List<ConnectorContext> allContexts = new ArrayList<>();

    CapSession originalSession = contentRepository.getConnection().getConnectionSession().activate();
    try {
      //execute with privileges
      ConnectorContextCacheKey cacheKey = new ConnectorContextCacheKey(contentRepository, settingsService, sitesService, getSiteConfigPath(), getGlobalConfigPath());
      allContexts.addAll(cache.get(cacheKey));

      ConnectorContextHomeCacheKey cacheKeyHome = new ConnectorContextHomeCacheKey(contentRepository, settingsService, getGlobalConfigPath());
      allContexts.addAll(cache.get(cacheKeyHome));
    } finally {
      //activate the original user session
      originalSession.activate();
    }

    return allContexts;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  public void setGlobalConfigPath(String globalConfigPath) {
    this.globalConfigPath = globalConfigPath;
  }

  public void setSiteConfigPath(String siteConfigPath) {
    this.siteConfigPath = siteConfigPath;
  }

  @Required
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  private String getGlobalConfigPath() {
    return StringUtils.isEmpty(globalConfigPath) ? DEFAULT_GLOBAL_CONFIGURATION_PATH : globalConfigPath;
  }

  private String getSiteConfigPath() {
    return StringUtils.isEmpty(siteConfigPath) ? DEFAULT_SITE_CONFIGURATION_PATH : siteConfigPath;
  }
}
