package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.library.toolbar.ConnectorRepositoryToolbarContainer;
import com.coremedia.blueprint.studio.connectors.model.Connector;
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.blueprint.studio.connectors.search.ConnectorSearchListContainer;
import com.coremedia.blueprint.studio.connectors.search.toolbar.ConnectorSearchToolbarContainer;
import com.coremedia.blueprint.studio.connectors.service.ConnectorService;
import com.coremedia.cap.common.SESSION;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.ContentTypeNames;
import com.coremedia.cap.content.search.SearchParameters;
import com.coremedia.cms.editor.sdk.ContentTreeRelation;
import com.coremedia.cms.editor.sdk.EditorContextImpl;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewExtension;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewModel;
import com.coremedia.cms.editor.sdk.collectionview.sort.RepositoryListSorter;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.cms.editor.sdk.upload.UploadManager;
import com.coremedia.cms.editor.sdk.upload.UploadSettings;
import com.coremedia.cms.editor.sdk.util.ContentLocalizationUtil;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpressionFactory;

import ext.Ext;

import mx.resources.ResourceManager;

public class ConnectorCollectionViewExtension implements CollectionViewExtension {

  protected static const DEFAULT_TYPE_RECORD:Object = {
    name: ContentTypeNames.DOCUMENT,
    label: ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'All_label'),
    icon: ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'All_icon')
  };

  public function ConnectorCollectionViewExtension() {
  }

  public function isApplicable(model:Object):Boolean {
    return model is ConnectorObject;
  }

  public function isUploadDisabledFor(folder:Object):Boolean {
    if (folder is ConnectorCategory) {
      return !(folder as ConnectorCategory).isWriteable();
    }
    return true;
  }

  public function upload(files:Array, folder:Object, settings:UploadSettings):void {
    if (files.length === 0) {
      return;
    }

    var category:ConnectorCategory = folder as ConnectorCategory;
    //update upload path
    var uri:String = category.getUploadUri();
    for each(var file:FileWrapper in files) {
      file.setCustomUploadUrl(uri);
    }

    var listView:ConnectorRepositoryList = Ext.getCmp(ConnectorRepositoryList.ID) as ConnectorRepositoryList;
    UploadManager.bulkUpload(settings, null, files, function (result:Array):void {
      listView.setDisabled(true);
      ValueExpressionFactory.createFromFunction(function ():Array {
        var items:Array = [];
        for each(var wrapper:FileWrapper in result) {
          var item:ConnectorItem = wrapper.getResultObject() as ConnectorItem;
          if (!item.isLoaded()) {
            item.load();
            return undefined;
          }
          items.push(item);
        }
        return items;
      }).loadValue(function (loadedResults:Array):void {
        for each(var item:ConnectorItem in loadedResults) {
          if (!item.getParent()) {
            trace('[ERROR]', "Parent model not set for connector item " + item.getUriPath());
            listView.setDisabled(false);
          }
          else {
            item.getParent().refresh(function ():void {
              listView.setDisabled(false);
            });
            return;
          }
        }
      });
    });
  }


  /**
   * Not based on content, therefore no ContentTreeRelation.
   * @return
   */
  public function getContentTreeRelation():ContentTreeRelation {
    return null;
  }

  public function getFolderContainerItemId():String {
    return ConnectorRepositoryListContainer.VIEW_CONTAINER_ITEM_ID;
  }

  public function getRepositoryToolbarItemId():String {
    return ConnectorRepositoryToolbarContainer.CONNECTOR_REPOSITORY_TOOLBAR_ITEM_ID;
  }

  public function getRepositoryListSorter():RepositoryListSorter {
    return null;
  }

  public function getSearchViewItemId():String {
    return ConnectorSearchListContainer.VIEW_CONTAINER_ITEM_ID;
  }

  public function getSearchToolbarItemId():String {
    return ConnectorSearchToolbarContainer.CONNECTOR_SEARCH_TOOLBAR_ITEM_ID;
  }

  public function getEnabledSearchFilterIds():Array {
    return null;
  }

  public function getAvailableSearchTypes(folder:Object):Array {
    if (folder is ConnectorObject) {
      var availableSearchTypes:Array = [DEFAULT_TYPE_RECORD];

      var connector:Connector = (folder as ConnectorObject).getConnector();
      var itemTypes:Array = connector.getItemTypes();
      if (itemTypes) {
        for each(var itemType:String in itemTypes) {
          var itemKey:String = itemType;
          if (itemKey.indexOf('/') !== -1) {
            itemKey = itemKey.substr(itemKey.indexOf('/') + 1, itemKey.length);
          }
          var recordSearchType:Object = {
            name: itemKey,
            label: ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'item_type_' + itemKey + '_name'),
            icon: ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', "item_type_" + itemKey + "_icon")
          };
          availableSearchTypes.push(recordSearchType);
        }
      }
      else if (connector.getConnectorType().indexOf(ConnectorHelper.TYPE_COREMEDIA_CONNECTOR) !== -1) {
        availableSearchTypes = [];
        var contentTypes:Array = SESSION.getConnection().getContentRepository().getContentTypes();
        for each(var ct:ContentType in contentTypes) {
          if (!ConnectorHelper.isIgnoredForSearch(ct)) {
            var recordSearchTypeCM:Object = {
              name: ct.getName(),
              label: ContentLocalizationUtil.getLabelForContentType(ct),
              icon: ContentLocalizationUtil.getIconStyleClassForContentType(ct)
            };
            availableSearchTypes.push(recordSearchTypeCM);
          }
        }
      }

      return availableSearchTypes;
    }
    return null;
  }

  public function getPathInfo(model:Object):String {
    var connectorObject:ConnectorEntity = model as ConnectorEntity;
    if (!connectorObject) {
      return "";
    }

    var namePath:Array = [];
    while (connectorObject) {
      namePath.push(connectorObject.getDisplayName());
      connectorObject = connectorObject.getParent();
    }

    return '/' + namePath.reverse().join('/');
  }

  public function search(searchParameters:SearchParameters, callback:Function):void {
    var connectionIds:String = getConnectorConnectionIds();
    var connectorType:String = getConnectorType();
    ConnectorService.search(connectorType, connectionIds, searchParameters, callback);
  }

  public function getSearchOrSearchSuggestionsParameters(filters:Object, mainStateBean:Bean):SearchParameters {
    var searchText:String = mainStateBean.get(CollectionViewModel.SEARCH_TEXT_PROPERTY);
    var searchType:String = mainStateBean.get(CollectionViewModel.CONTENT_TYPE_PROPERTY);
    var searchParameters:SearchParameters = SearchParameters({});
    delete searchParameters['xclass'];
    var catalogObject:ConnectorObject = mainStateBean.get(CollectionViewModel.FOLDER_PROPERTY);
    if (catalogObject is ConnectorCategory) {
      searchParameters['category'] = catalogObject.getConnectorId();
    }
    searchParameters.query = searchText || "*";
    searchParameters['searchType'] = searchType;
    return searchParameters;
  }

  private static function getConnectorType():String {
    var collectionViewModel:CollectionViewModel = EditorContextImpl(editorContext).getCollectionViewModel();
    var mainStateBean:Bean = collectionViewModel.getMainStateBean();
    var selection:ConnectorObject = mainStateBean.get(CollectionViewModel.FOLDER_PROPERTY);
    if (selection is Connector) {
      return (selection as Connector).getConnectorType();
    }
    return (selection as ConnectorEntity).getConnector().getConnectorType();
  }

  private static function getConnectorConnectionIds():String {
    var collectionViewModel:CollectionViewModel = EditorContextImpl(editorContext).getCollectionViewModel();
    var mainStateBean:Bean = collectionViewModel.getMainStateBean();
    var selection:ConnectorObject = mainStateBean.get(CollectionViewModel.FOLDER_PROPERTY);
    if (selection is Connector) {
      return "all";
    }
    return (selection as ConnectorEntity).getConnectionId();
  }

  public function isSearchable():Boolean {
    return true;
  }

  public function getSearchSuggestionsUrl():String {
    return ConnectorService.getSearchSuggestionsUrl(getConnectorType(), getConnectorConnectionIds());
  }
}
}
