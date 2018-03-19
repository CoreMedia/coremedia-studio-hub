package com.coremedia.blueprint.studio.connectors.notifications {

import com.coremedia.blueprint.studio.connectors.library.ConnectorRepositoryList;
import com.coremedia.cms.editor.notification.actions.NotificationAction;

import ext.Ext;

[ResourceBundle('com.coremedia.blueprint.studio.connectors.ConnectorNotifications')]
public class ShowConnectorForNotificationActionBase extends NotificationAction {

  public function ShowConnectorForNotificationActionBase(config:ShowConnectorForNotificationAction = null) {
    super(config);
    registerActionStateHandler(ACTION_STATE_EXECUTABLE, showConnectorEntity);
  }

  private function showConnectorEntity():void {
    var connectorId:String = getNotification().getParameters()[0] as String;
    if (connectorId) {
      var listView:ConnectorRepositoryList = Ext.getCmp(ConnectorRepositoryList.ID) as ConnectorRepositoryList;
      listView.showItem(connectorId);
    }
  }
}
}
