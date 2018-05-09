package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Caching of the Connections.xml
 */
public class ConnectorContextCacheKey extends CacheKey<List<ConnectorContext>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorContextCacheKey.class);

  static final String CONNECTIONS_DOCUMENT = "Connections";
  static final String CONNECTIONS_STRUCT = "connections";

  private static final String TYPES_DOCUMENT = "Connector Types";
  private static final String TYPES_STRUCT = "types";
  private static final String CONNECTOR_TYPE = "connectorType";

  ContentRepository contentRepository;
  private SettingsService settingsService;
  private SitesService sitesService;
  private String siteConfigPath;
  private String globalConfigPath;

  ConnectorContextCacheKey(ContentRepository contentRepository,
                                   SettingsService settingsService,
                                   SitesService sitesService,
                                   String siteConfigPath,
                                   String globalConfigPath) {
    this.contentRepository = contentRepository;
    this.settingsService = settingsService;
    this.sitesService = sitesService;
    this.siteConfigPath = siteConfigPath;
    this.globalConfigPath = globalConfigPath;
  }

  @Override
  public List<ConnectorContext> evaluate(Cache cache) {
    List<ConnectorContext> result = new ArrayList<>();

    //site specific connections
    if (siteConfigPath != null) {
      Set<Site> sites = sitesService.getSites();
      for (Site s : sites) {
        createContextsForSite(result, s);
      }
    }

    //Global connections
    if (globalConfigPath != null) {
      List globalSettings = findConnectorSettings(globalConfigPath + "/" + CONNECTIONS_DOCUMENT, CONNECTIONS_STRUCT);
      for (Object setting : globalSettings) {
        if (setting instanceof Struct) {
          result.add(new ConnectorContextImpl(((Struct) setting).getProperties()));
        }
      }
    }

    return updateContextsWithConnectorType(result);
  }

  /**
   * The connection only references the connector type which itself has some additional settings linked.
   * These settings are applied to each connection, like the preview templates or the item types
   */
  List<ConnectorContext> updateContextsWithConnectorType(List<ConnectorContext> result) {
    List<ConnectorContext> filteredList = new ArrayList<>();

    //finally apply connector types data
    List connectorTypes = findConnectorSettings(globalConfigPath + "/" + TYPES_DOCUMENT, TYPES_STRUCT);
    Map<String, Struct> connectorTypeMapping = new HashMap<>();
    for (Object connectorType : connectorTypes) {
      if (connectorType instanceof Struct) {
        Struct connectorTypeStruct = (Struct) connectorType;
        connectorTypeMapping.put(connectorTypeStruct.getString(CONNECTOR_TYPE), connectorTypeStruct);
      }
    }

    for (ConnectorContext connectorContext : result) {
      ConnectorContextImpl context = (ConnectorContextImpl) connectorContext;

      if (!connectorTypeMapping.containsKey(connectorContext.getType())) {
        LOGGER.error("Could not find connector type '" + connectorContext.getType() + "' is list of global connector types.");
        continue;
      }

      //merge type information into context
      Struct struct = connectorTypeMapping.get(connectorContext.getType());
      context.getProperties().putAll(struct.getProperties());

      //apply additional settings if not already applied to the connection itself
      if (struct.get(ConnectorContextImpl.ITEM_TYPES) != null && context.getItemTypes() == null) {
        context.setItemTypes(new ConnectorItemTypesImpl(struct.getLink(ConnectorContextImpl.ITEM_TYPES)));
      }
      if (struct.get(ConnectorContextImpl.PREVIEW_TEMPLATES) != null && context.getPreviewTemplates() == null) {
        context.setPreviewTemplates(new ConnectorPreviewTemplatesImpl(struct.getLink(ConnectorContextImpl.PREVIEW_TEMPLATES)));
      }

      if (struct.get(ConnectorContextImpl.CONTENT_MAPPING) != null) {
        context.setContentMapping(new ConnectorContentMappingsImpl(struct.getLink(ConnectorContextImpl.CONTENT_MAPPING)));
      }

      filteredList.add(connectorContext);
    }

    return filteredList;
  }

  private void createContextsForSite(List<ConnectorContext> result, Site site) {
    Content siteRootFolder = site.getSiteRootFolder();
    List connectionSettings = findConnectorSettings(siteRootFolder.getPath()
            + siteConfigPath + "/" + CONNECTIONS_DOCUMENT, CONNECTIONS_STRUCT);
    for (Object setting : connectionSettings) {
      if (setting instanceof Struct) {
        ConnectorContextImpl connectorContext = new ConnectorContextImpl(((Struct) setting).getProperties());
        connectorContext.setSiteId(site.getId());
        result.add(connectorContext);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ConnectorContextCacheKey)) {
      return false;
    }

    ConnectorContextCacheKey that = (ConnectorContextCacheKey) o;
    return getCacheIdentifier().equals(that.getCacheIdentifier());
  }

  @Override
  public int hashCode() {
    return getCacheIdentifier().hashCode();
  }

  //----------------------- Helper -------------------------------------------------------------------------------------

  private String getCacheIdentifier() {
    return String.valueOf(siteConfigPath) + ":" + String.valueOf(globalConfigPath);
  }

  List findConnectorSettings(String location, String structId) {
    Content settings = contentRepository.getChild(location);
    if (settings != null) {
      return settingsService.settingWithDefault(structId, List.class, Collections.emptyList(), settings.get("settings"));
    }
    return Collections.emptyList();
  }
}
