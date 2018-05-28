package com.coremedia.blueprint.studio.connectors.dnd {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.service.ConnectorContentCreationResult;
import com.coremedia.blueprint.studio.connectors.service.ConnectorContentService;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.collectionview.CollectionView;
import com.coremedia.cms.editor.sdk.collectionview.tree.LibraryTree;
import com.coremedia.cms.editor.sdk.dragdrop.DragInfo;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.premular.fields.plugins.LibraryTreeViewDragDropPlugin;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.AccessControlUtil;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.skins.LoadMaskSkin;

import ext.Component;
import ext.Ext;
import ext.LoadMask;
import ext.Plugin;
import ext.dd.DragSource;
import ext.dd.DropTarget;
import ext.event.Event;
import ext.view.AbstractView;
import ext.view.TableView;

import js.HTMLElement;

import mx.resources.ResourceManager;

/**
 * A drop zone for property editors of link list properties
 */
public class ConnectorContentTreeDropAreaTarget extends DropTarget {

  private var libraryTree:LibraryTree;
  private var loadMask:LoadMask;
  private var tableView:TableView;
  private var treeDnDPlugin:LibraryTreeViewDragDropPlugin;
  private var folderBlacklist:Array;

  public function ConnectorContentTreeDropAreaTarget(dropArea:Component, view:TableView, folderBlacklist:Array) {
    super(dropArea.getEl(), DropTarget({
      ddGroup: "ConnectorDD"
    }));
    this.folderBlacklist = folderBlacklist;

    var loadMaskCfg:LoadMask = LoadMask({
      target: dropArea,
      ui: LoadMaskSkin.LIGHT.getSkin()
    });
    loadMaskCfg.msg = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_busy_message');
    loadMaskCfg.ui = LoadMaskSkin.OPAQUE.getSkin();
    loadMaskCfg.target = Ext.getCmp(CollectionView.COLLECTION_VIEW_ID);
    loadMask = new LoadMask(loadMaskCfg);
    loadMask.disable();

    this.tableView = view;
    this.libraryTree = dropArea as LibraryTree;
    var plugins:Array = this.tableView['getPlugins']();
    for each(var plugin:Plugin in plugins) {
      if (plugin is LibraryTreeViewDragDropPlugin) {
        treeDnDPlugin = plugin as LibraryTreeViewDragDropPlugin;
        break;
      }
    }
  }

  private function allowDrop(dragInfo:DragInfo, e:Event, fromView:AbstractView):Boolean {
    if (!dragInfo || libraryTree.disabled) {
      return false;
    }

    var items:Array = dragInfo.getContents();
    for each(var item:ConnectorEntity in items) {
      if (!(item is ConnectorItem) && !(item is ConnectorCategory)) {
        return false;
      }
    }

    var content:Content = getDropTarget(e);
    if (content && content.isFolder() && !AccessControlUtil.isReadOnly(content)) {
      var path:String = content.getPath();
      for each(var blacklistEntry:String in folderBlacklist) {
        if (path.indexOf(blacklistEntry) !== -1) {
          return false;
        }
      }

      return true;
    }
    return false;
  }

  override public function notifyEnter(source:DragSource, e:Event, data:Object):String {
    var dragInfo:DragInfo = DragInfo.makeDragInfo(data);
    var mayDrop:Boolean = allowDrop(dragInfo, e, data.view);
    return mayDrop ? dropAllowed : dropNotAllowed;
  }

  override public function notifyOver(source:DragSource, e:Event, data:Object):String {
    var dragInfo:DragInfo = DragInfo.makeDragInfo(data);
    var mayDrop:Boolean = allowDrop(dragInfo, e, data.view);
    return mayDrop ? dropAllowed : dropNotAllowed;
  }

  override public function notifyDrop(source:DragSource, e:Event, data:Object):Boolean {
    var dragInfo:DragInfo = DragInfo.makeDragInfo(data);
    var mayDrop:Boolean = allowDrop(dragInfo, e, data.view);
    if (mayDrop) {
      handleDrop(dragInfo.getContents(), e);
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

  private function handleDrop(items:Array, e:Event):void {
    setBusy(true);

    var targetFolder:Content = getDropTarget(e);
    var folder:String = targetFolder.getPath();
    var site:Site = editorContext.getSitesService().getSiteFor(targetFolder);
    //create the content first
    ConnectorContentService.createContentsForDrop(items, site, function (createdContents:Array):void {
      if (createdContents.length === 0) {
        setBusy(false);
        return;
      }

      for each(var result:ConnectorContentCreationResult in createdContents) {
        //fire write interceptors for new contents
        if(result.isNew) {
          ConnectorContentService.processContent(result.content, result.connectorEntity, function ():void {
            setBusy(false);
          });
        }
      }

      setBusy(false);
      for each(var cr:ConnectorContentCreationResult in createdContents) {
        if (!cr.isNew) {
          var title:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'duplicate_title');
          var msg:String = ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'duplicate_create_canceled_message');
          MessageBoxUtil.showInfo(title, msg);
          return;
        }
      }
    }, folder);
  }

  private function getDropTarget(e:Event):Content {
    var targetNode:HTMLElement = HTMLElement(treeDnDPlugin.dropZone.getTargetFromEvent(e));
    if(!targetNode) {
      return null;
    }

    var recordId:String = targetNode.getAttribute("data-recordindex") as String;
    var beanId:String = libraryTree.getStore().getAt(Number(recordId)).getId();
    return beanFactory.getRemoteBean(beanId) as Content;
  }
}
}

