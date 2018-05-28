package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * The connector context contains all connection information for the external system,
 * like credentials and additional configuration parameters, like the folder for a file system based connector.
 */
public interface ConnectorContext {

  /**
   * Returns the connectionId of the connection this context belongs too.
   * This id must be unique over all sites.
   */
  @Nonnull
  String getConnectionId();

  /**
   * Returns the connector type for this context, as defined in the Connections.xml
   */
  @Nonnull
  String getType();

  /**
   * Returns the name that has been set for the connector type name in 'Connector Types'.
   */
  @Nonnull
  String getTypeName();

  /**
   * Utility methods to access additional configuration parameters.
   */
  String getProperty(String key);
  Boolean getBooleanProperty(String key, boolean defaultValue);

  /**
   * Returns the threshold im MB that is used for the item preview.
   * Since some items are not streamed, but downloaded for the preview this ensures that the preview
   * ignores files that are simply to large.
   *
   * If the system if files based, it's recommended to set the corresponding value in the connection settings.
   */
  int getPreviewThresholdMB();

  /**
   * Allows to turn the connection on/off.
   */
  boolean isEnabled();

  /**
   * Returns the scope to use during the content creation.
   * The scope defines what folder should be checked to avoid duplicate content creation.
   * Possible values are 'global', 'site' and 'domain'.
   * @return the scope defined in the settings or 'global' is no such property was set
   */
  String getContentScope();

  /**
   * Returns true if items in the list view should be marked as read.
   * Defaults to false.
   */
  boolean isMarkAsReadEnabled();

  /**
   * Returns the invalidation interval in seconds.
   * If no value or 0 is configured, the invalidation will be skipped.
   */
  long getInvalidationInterval();

  /**
   * Returns the list of user groups that are notified for this connection context.
   * @return a list with cap group names
   */
  @Nonnull
  List<String> getNotificationsUserGroups();

  /**
   * Returns a list of user names that are notified for this connection context.
   * @return a list with cap user names
   */
  @Nonnull
  List<String> getNotificationsUsers();

  /**
   * Returns the item types this connector provides.
   * The list may be null if no configuration is given.
   * This may be the case if the connector only supports one item type.
   * In that case, the type may be defined by implementing the method <code>getItemType()</code>
   * of the ConnectorItem interface.
   *
   * @see ConnectorItem#getItemType()
   */
  @Nullable
  ConnectorItemTypes getItemTypes();

  /**
   * Returns the preview templates this connector provides.
   * Usually the default configuration is applied here since most preview templates won't differ.
   * If no configuration given, the template can be provided by implemeting the method <code>getPreviewHtml()</code>
   * of the ConnectorItem interface.
   *
    @see ConnectorItem#getPreviewHtml()
   */
  @Nullable
  ConnectorPreviewTemplates getPreviewTemplates();

  /**
   * Returns the connector content mappings for this connector.
   * Usually the provided default configuration can be applied here.
   * This determines which content to create for a connector item type.
   * Since the content creation is based on ContentWriteInterceptors, additional content be
   * created, depending on the data of the connector.
   */
  @Nullable
  ConnectorContentMappings getContentMappings();

  /**
   * Returns the locale that should be used for this connector.
   * The locale matches the one that is set for the current Studio user.
   */
  @Nonnull
  Locale getLocale();

  /**
   * Returns the type of date format that should be used when displayed in the Studio.
   * Possible values are 'long' and 'short'.
   */
  String getDateFormat();

  /**
   * Returns the default columns to show in the library list view.
   * Custom columns are not affected by this.
   * @return the list of default columns to display.
   */
  @Nonnull
  List<String> getDefaultColumns();
}
