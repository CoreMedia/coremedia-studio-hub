package com.coremedia.blueprint.studio.connectors.service {
import com.coremedia.blueprint.studio.connectors.model.*;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.coremedia.ui.util.ObjectUtils;

import ext.JSON;
import ext.StringUtil;

import mx.resources.ResourceManager;

/**
 * Utility class for accessing the ConnectorContentServiceResource
 */
public class ConnectorContentService {

  public static function createContentsForDrop(connectorItems:Array, callback:Function, folder:String):void {
    var results:Array = [];
    var count:Number = 0;
    for each(var item:ConnectorItem in connectorItems) {
      createContentForItem(item, folder, function (creationResult:ConnectorContentCreationResult):void {
        count++;
        if(creationResult) {
          results.push(creationResult);
        }
        if (count == connectorItems.length) {
          callback.call(null, results);
        }
      });
    }
  }

  public static function findContent(item:ConnectorItem, callback:Function):void {
    var url:String = 'connector/contentservice/content/' + editorContext.getSitesService().getPreferredSiteId();
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var params:* = {
      id: item.getConnectorId()
    };
    remoteServiceMethod.request(params, function (response:RemoteServiceMethodResponse):void {
      var contentId:String = response.response.responseText;
      if (contentId) {
        var content:Content = BeanFactoryImpl.resolveBeans(JSON.decode(contentId)) as Content;
        callback.call(null, content);
      }
      else {
        callback.call(null, null);
      }
    });
  }

  private static function createContentForItem(item:ConnectorItem, folder:String, callback:Function):void {
    var url:String = 'connector/contentservice/create/' + editorContext.getSitesService().getPreferredSiteId();
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var params:* = {
      folder: folder,
      id: item.getConnectorId()
    };

    remoteServiceMethod.request(params, function (response:RemoteServiceMethodResponse):void {
      var id:String = response.response.responseText;
      if (id) {
        var content:Content = BeanFactoryImpl.resolveBeans(JSON.decode(id)) as Content;
        content.load(function ():void {
          var result:ConnectorContentCreationResult = new ConnectorContentCreationResult(content, item);
          callback.call(null, result);
        });
      }
      else {
        callback.call(null, null);
      }
    });
  }

  public static function processContent(content:Content, item:ConnectorItem, callback:Function, wait:Boolean = false):void {
    if (content.isCheckedOut()) {
      content.checkIn(function ():void {
        invokePostProcessing(content, item, callback, wait);
      });
    }
    else {
      invokePostProcessing(content, item, callback, wait);
    }
  }

  private static function invokePostProcessing(content:Content, item:ConnectorItem, callback:Function, wait:Boolean):void {
    var url:String = 'connector/contentservice/process/' + editorContext.getSitesService().getPreferredSiteId();
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var params:* = {
      contentId: content.getId(),
      id: item.getConnectorId()
    };
    remoteServiceMethod.request(params, function (response:RemoteServiceMethodResponse):void {
      var errorMessage:String = response.response.responseText;
      if (errorMessage) {
        var title:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_title');
        var message:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_error_message');
        message = StringUtil.format(message, errorMessage);
        MessageBoxUtil.showError(title, message);
      }
      else {
        if (callback) {
          if(wait) {
            waitForFeeding(content, item, 100, callback);
          }
          else {
            callback(content)
          }
        }
      }
    }, function (response:RemoteServiceMethodResponse):void {
      MessageBoxUtil.showInfo("Error creating content", response.getError().message);
    });
  }

  /**
   * Increased waiting until a content is found
   * @param content
   * @param item
   * @param timeout
   * @param callback
   */
  private static function waitForFeeding(content:Content, item:ConnectorItem, timeout:Number, callback:Function):void {
    findContent(item, function(feededContent:Content):void {
      if(feededContent) {
        callback(content);
      }
      else {
        window.setTimeout(function():void {
          timeout = timeout > 10000 ? timeout : timeout*2;
          waitForFeeding(content, item, timeout, callback);
        }, timeout);
      }
    });
  }
}
}