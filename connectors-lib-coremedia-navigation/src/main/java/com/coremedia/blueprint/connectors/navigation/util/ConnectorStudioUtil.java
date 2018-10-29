package com.coremedia.blueprint.connectors.navigation.util;

/**
 *
 */
public class ConnectorStudioUtil {

  public static String generateOpenEntityLink(String uriPath, String name) {
    return "<a href=\"javascript:Ext.getCmp('collection-view').showInRepositoryMode(com.coremedia.ui.data.beanFactory.getRemoteBean('" + uriPath + "'))\">" + name + "</a>";
  }
}
