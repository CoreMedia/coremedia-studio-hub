package com.coremedia.blueprint.connectors.impl;

import com.coremedia.blueprint.connectors.api.ConnectorContentMappings;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorItemTypes;
import com.coremedia.blueprint.connectors.api.ConnectorPreviewTemplates;
import com.coremedia.cap.content.Content;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConnectorContextImpl implements ConnectorContext {

  public static final String MARK_AS_READ = "markAsRead";
  public static final String PREVIEW_THRESHOLD = "previewThresholdMB";
  public static final String CONTENT_SCOPE= "contentScope";
  public static final String TYPE = "type";
  public static final String CONNECTION_ID = "connectionId";
  public static final String DATE_FORMAT = "dateFormat";

  private static final String ENABLED = "enabled";

  public static final String INVALIDATION_INTERVAL = "invalidationInterval";
  public static final String NOTIFICATION_GROUPS = "notificationGroups";
  public static final String NOTIFICATION_USERS = "notificationUsers";

  public static final String CONTENT_SCOPE_SITE = "site";
  public static final String CONTENT_SCOPE_GLOBAL = "global";
  public static final String CONTENT_SCOPE_DOMAIN = "domain";

  static final String ITEM_TYPES = "itemTypes";
  static final String PREVIEW_TEMPLATES = "previewTemplates";
  static final String CONTENT_MAPPING = "contentMapping";

  private Map<String, Object> properties;

  private ConnectorItemTypes itemTypes;
  private ConnectorContentMappings contentMapping;
  private ConnectorPreviewTemplates previewTemplates;

  private boolean dirty;
  private String siteId;
  private Locale locale;

  public ConnectorContextImpl(Map<String, Object> properties) {
    this.properties = new HashMap<>(properties);

    if (properties.get(ITEM_TYPES) != null) {
      this.itemTypes = new ConnectorItemTypesImpl((Content) properties.get(ITEM_TYPES));
    }
    if (properties.get(PREVIEW_TEMPLATES) != null) {
      this.previewTemplates = new ConnectorPreviewTemplatesImpl((Content) properties.get(PREVIEW_TEMPLATES));
    }
    if (properties.get(CONTENT_MAPPING) != null) {
      this.contentMapping = new ConnectorContentMappingsImpl((Content) properties.get(CONTENT_MAPPING));
    }

    setDirty(true);
  }

  @Override
  public long getInvalidationInterval() {
    if(properties.containsKey(INVALIDATION_INTERVAL)) {
      Integer interval = (Integer) properties.get(INVALIDATION_INTERVAL);
      if(interval != null) {
        return interval.longValue();
      }
    }
    return 0;
  }

  @Nonnull
  @Override
  public List<String> getNotificationsUserGroups() {
    if(properties.containsKey(NOTIFICATION_GROUPS)) {
      String groupsString = (String) properties.get(NOTIFICATION_GROUPS);
      String[] split = groupsString.split(",");
      return Arrays.asList(split);
    }
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<String> getNotificationsUsers() {
    if(properties.containsKey(NOTIFICATION_USERS)) {
      String usersString = (String) properties.get(NOTIFICATION_USERS);
      String[] split = usersString.split(",");
      return Arrays.asList(split);
    }
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public String getConnectionId() {
    return (String) properties.get(CONNECTION_ID);
  }

  @Nonnull
  @Override
  public String getType() {
    return (String) properties.get(TYPE);
  }

  @Override
  public String getProperty(String key) {
    return (String) properties.get(key);
  }

  @Override
  public int getPreviewThresholdMB() {
    if (properties.containsKey(PREVIEW_THRESHOLD)) {
      return (Integer) properties.get(PREVIEW_THRESHOLD);
    }
    return -1;
  }

  @Override
  public String getDateFormat() {
    return (String) properties.get(DATE_FORMAT);
  }

  @Override
  public boolean isEnabled() {
    if (properties.containsKey(ENABLED)) {
      return (Boolean) properties.get(ENABLED);
    }
    return true;
  }

  @Override
  public boolean isMarkAsReadEnabled() {
    if (properties.containsKey(MARK_AS_READ)) {
      return (Boolean) properties.get(MARK_AS_READ);
    }
    return false;
  }

  @Override
  public String getContentScope() {
    if (properties.containsKey(CONTENT_SCOPE)) {
      return (String) properties.get(CONTENT_SCOPE);
    }
    return null;
  }

  @Override
  public ConnectorItemTypes getItemTypes() {
    return itemTypes;
  }

  @Nullable
  @Override
  public ConnectorPreviewTemplates getPreviewTemplates() {
    return previewTemplates;
  }

  @Nullable
  @Override
  public ConnectorContentMappings getContentMappings() {
    return contentMapping;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  void setItemTypes(ConnectorItemTypes itemTypes) {
    this.itemTypes = itemTypes;
  }

  void setPreviewTemplates(ConnectorPreviewTemplates previewTemplates) {
    this.previewTemplates = previewTemplates;
  }

  void setContentMapping(ConnectorContentMappings contentMapping) {
    this.contentMapping = contentMapping;
  }

  boolean isDirty() {
    return dirty;
  }

  void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  @Nonnull
  @Override
  public Locale getLocale() {
    if(locale == null) {
      return Locale.getDefault();
    }
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }
}
