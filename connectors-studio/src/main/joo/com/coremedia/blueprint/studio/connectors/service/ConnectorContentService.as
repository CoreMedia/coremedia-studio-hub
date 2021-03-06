package com.coremedia.blueprint.studio.connectors.service {
import com.coremedia.blueprint.studio.connectors.model.*;
import com.coremedia.blueprint.studio.connectors.model.ConnectorContext;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

import ext.JSON;
import ext.StringUtil;

import mx.resources.ResourceManager;

/**
 * Utility class for accessing the ConnectorContentServiceResource
 */
public class ConnectorContentService {

  public static function createContentsForDrop(connectorEntities:Array, site:Site, callback:Function, folder:String):void {
    ValueExpressionFactory.createFromFunction(function():ConnectorContext {
      var ctx:ConnectorContext = connectorEntities[0].getContext();
      if(ctx === undefined) {
        return undefined;
      }
      return ctx;
    }).loadValue(function(context:ConnectorContext):void {
      var scope:String = context.getContentScope();
      if (!scope) {
        createContents(connectorEntities, callback, folder);
        return;
      }

      var results:Array = [];
      var count:Number = 0;
      for each(var entity:ConnectorEntity in connectorEntities) {
        findContent(entity, folder, site, function (searchEntity:ConnectorEntity, existingContent:Content):void {
          if(existingContent) {
            count++;
            var creationResult:ConnectorContentCreationResult = new ConnectorContentCreationResult(existingContent, searchEntity, false);
            results.push(creationResult);

            if (count == connectorEntities.length) {
              callback.call(null, results);
            }
          }
          else {
            createContentForItem(searchEntity, folder, function (cr:ConnectorContentCreationResult):void {
              count++;
              results.push(cr);
              if (count == connectorEntities.length) {
                callback.call(null, results);
              }
            });
          }

        });
      }
    });
  }

  private static function createContents(connectorEntities:Array, callback:Function, folder:String):void {
    var results:Array = [];
    var count:Number = 0;
    for each(var entity:ConnectorEntity in connectorEntities) {
      createContentForItem(entity, folder, function (creationResult:ConnectorContentCreationResult):void {
        count++;
        if (creationResult) {
          results.push(creationResult);
        }
        if (count == connectorEntities.length) {
          callback.call(null, results);
        }
      });
    }
  }

  public static function findContent(entity:ConnectorEntity, folder:String, site:Site, callback:Function):void {
    var siteId:String = '';
    if(site) {
      siteId = site.getId();
    }

    var url:String = 'connector/contentservice/content/' + siteId;
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var params:* = {
      id: entity.getConnectorId(),
      folder : folder
    };
    remoteServiceMethod.request(params, function (response:RemoteServiceMethodResponse):void {
      var contentId:String = response.response.responseText;
      if (contentId) {
        var content:Content = BeanFactoryImpl.resolveBeans(JSON.decode(contentId)) as Content;
        callback.call(null, entity, content);
      }
      else {
        callback.call(null, entity, null);
      }
    });
  }

  private static function createContentForItem(entity:ConnectorEntity, folder:String, callback:Function):void {
    var url:String = 'connector/contentservice/create/' + editorContext.getSitesService().getPreferredSiteId();
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var params:* = {
      folder: folder,
      id: entity.getConnectorId()
    };

    remoteServiceMethod.request(params, function (response:RemoteServiceMethodResponse):void {
      var id:String = response.response.responseText;
      if (id) {
        var content:Content = BeanFactoryImpl.resolveBeans(JSON.decode(id)) as Content;
        content.load(function (loadedContent:Content):void {
          var result:ConnectorContentCreationResult = new ConnectorContentCreationResult(loadedContent, entity, true);
          callback.call(null, result);
        });
      }
      else {
        callback.call(null, null);
      }
    });
  }

  public static function processContent(content:Content, entity:ConnectorEntity, callback:Function, wait:Boolean = false):void {
    if (content.isCheckedOut()) {
      content.checkIn(function ():void {
        invokePostProcessing(content, entity, callback, wait);
      });
    }
    else {
      invokePostProcessing(content, entity, callback, wait);
    }
  }

  private static function invokePostProcessing(content:Content, entity:ConnectorEntity, callback:Function, wait:Boolean):void {
    var url:String = 'connector/contentservice/process/' + editorContext.getSitesService().getPreferredSiteId();
    var remoteServiceMethod:RemoteServiceMethod = new RemoteServiceMethod(url, 'POST');
    var params:* = {
      contentId: content.getId(),
      id: entity.getConnectorId()
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
          if (wait) {
            waitForFeeding(content, entity, 100, callback);
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
   * @param entity
   * @param timeout
   * @param callback
   */
  private static function waitForFeeding(content:Content, entity:ConnectorEntity, timeout:Number, callback:Function):void {
    var site:Site = editorContext.getSitesService().getSiteFor(content);
    findContent(entity, null, site, function (searchedEntity:ConnectorEntity, feededContent:Content):void {
      if (feededContent) {
        callback(content);
      }
      else {
        window.setTimeout(function ():void {
          if(timeout > 10000) {
            content.doDelete();
            var title:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_title');
            var message:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_error_message_feeding_error');
            MessageBoxUtil.showError(title, message);
          }
          else {
            timeout = timeout * 2;
            waitForFeeding(content, entity, timeout, callback);
          }
        }, timeout);
      }
    });
  }
}
}