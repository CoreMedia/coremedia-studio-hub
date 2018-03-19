package com.coremedia.blueprint.studio.connectors.dnd {
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.service.ConnectorContentCreationResult;
import com.coremedia.blueprint.studio.connectors.service.ConnectorContentService;
import com.coremedia.blueprint.studio.connectors.service.ConnectorService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cms.editor.sdk.dragdrop.DragInfo;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.util.ContentLinkListWrapper;
import com.coremedia.ui.skins.LoadMaskSkin;

import ext.Component;
import ext.LoadMask;
import ext.dd.DragSource;
import ext.dd.DropTarget;
import ext.event.Event;
import ext.view.AbstractView;

import mx.resources.ResourceManager;

/**
 * A drop zone for property editors of link list properties
 */
public class ConnectorLinkListDropAreaTarget extends DropTarget {

  private var dropArea:Component;
  private var boundView:AbstractView;
  private var linkListWrapper:ContentLinkListWrapper;
  private var llOwner:Content;
  private var loadMask:LoadMask;

  public function ConnectorLinkListDropAreaTarget(dropArea:Component, boundView:AbstractView, linkListWrapper:ContentLinkListWrapper) {
    super(dropArea.getEl(), DropTarget({
      ddGroup: "ConnectorDD"
    }));

    this.dropArea = dropArea;
    this.linkListWrapper = linkListWrapper;
    this.boundView = boundView;

    llOwner = linkListWrapper.bindTo.getValue();

    var loadMaskCfg:LoadMask = LoadMask({
      target: dropArea,
      ui: LoadMaskSkin.LIGHT.getSkin()
    });
    loadMaskCfg.msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_busy_message');
    loadMask = new LoadMask(loadMaskCfg);
    loadMask.disable();

    // Lock drop target until the bean property is fully loaded.
    lock();
    linkListWrapper.getVE().loadValue(function ():void {
      unlock();
    });
  }

  private function allowDrop(dragInfo:DragInfo, fromView:AbstractView):Boolean {
    if (!dragInfo || dropArea.disabled || !llOwner || llOwner.isCheckedOutByOther()) {
      return false;
    }

    var items:Array = dragInfo.getContents();
    var llContentType:ContentType = linkListWrapper.getContentType();

    for each(var item:ConnectorItem in items) {
      if (!item) {
        continue;
      }
      var itemType:String = item.getItemType();
      var targetContentType:String = item.getConnector().getContentMappings().getMapping(itemType);
      if (!targetContentType) {
        return false;
      }

      var contentType:ContentType = editorContext.getSession().getConnection().getContentRepository().getContentType(targetContentType);
      if (!contentType) {
        trace('[ERROR]', 'The content repository does not provide the content type "' + targetContentType + '"');
        return false;
      }

      if (!contentType.isSubtypeOf(llContentType.getName())) {
        return false;
      }
    }

    return (boundView !== null && boundView === fromView) || linkListWrapper.getFreeCapacity() >= dragInfo.getContents().length;
  }

  override public function notifyEnter(source:DragSource, e:Event, data:Object):String {
    var dragInfo:DragInfo = DragInfo.makeDragInfo(data);
    var mayDrop:Boolean = allowDrop(dragInfo, data.view);
    return mayDrop ? dropAllowed : dropNotAllowed;
  }

  override public function notifyOver(source:DragSource, e:Event, data:Object):String {
    var dragInfo:DragInfo = DragInfo.makeDragInfo(data);
    var mayDrop:Boolean = allowDrop(dragInfo, data.view);
    return mayDrop ? dropAllowed : dropNotAllowed;
  }

  override public function notifyDrop(source:DragSource, e:Event, data:Object):Boolean {
    var dragInfo:DragInfo = DragInfo.makeDragInfo(data);
    var mayDrop:Boolean = allowDrop(dragInfo, data.view);
    if (mayDrop) {
      handleDrop(dragInfo.getContents());
    }
    return mayDrop;
  }

  override public function notifyOut(source:DragSource, e:Event, data:Object):void {
    super.notifyOut(source, e, data);
  }

  private function setBusy(busy:Boolean):void {
    if (busy) {
      loadMask.show();
    }
    else {
      loadMask.hide();
    }
  }

  private function handleDrop(items:Array):void {
    setBusy(true);
    var itemsToCreate:Array = [];
    for each(var item:ConnectorItem in items) {
      if (!item) {
        continue;
      }
      itemsToCreate.push(item);
    }

    var folder:String = llOwner.getParent().getPath();
    //create the content first
    ConnectorContentService.createContentsForDrop(itemsToCreate, function (createdContents:Array):void {
      //link it to the content...
      var count:Number = createdContents.length;
      //..and trigger the server side post processing

      if(count > 0) {
        for each(var result:ConnectorContentCreationResult in createdContents) {
          ConnectorContentService.processContent(result.content, result.connectorItem, function ():void {
            count--;
            if(count === 0) {
              findAndLinkContents(itemsToCreate);
            }
          }, true);
        }
      }
      else {
        findAndLinkContents(itemsToCreate);
      }
    }, folder);
  }

  private function findAndLinkContents(itemsToCreate:Array):void {
    var contents:Array = [];
    for each(var item:ConnectorItem in itemsToCreate) {
      ConnectorContentService.findContent(item, function(content:Content):void {
        if(content) {
          content.load(function():void {
            contents.push(content);
            if(contents.length === itemsToCreate.length) {
              link(contents, function(results:Array):void {
                setBusy(false);
              });
            }
          });
        }
        else {
          trace('[ERROR]', "Failed to find content for item "+ item.getConnectorId());
        }
      });
    }
  }


  private function link(createdContents:Array, callback:Function):void {
    var contents:Array = [];
    var newList:Array = linkListWrapper.getLinks().concat(createdContents);
    if (!llOwner.isCheckedOut()) {
      llOwner.checkOut(function ():void {
        llOwner.invalidate(function ():void {
          linkListWrapper.setLinks(newList);
          callback.call(null, createdContents);
        });
      });
    }
    else {
      linkListWrapper.setLinks(newList);
      callback.call(null, createdContents);
    }
  }
}
}

