package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.blueprint.base.components.quickcreate.QuickCreateDialog;
import com.coremedia.blueprint.studio.connectors.helper.ContentCreationHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.blueprint.studio.connectors.model.ConnectorObject;
import com.coremedia.blueprint.studio.connectors.service.ConnectorContentService;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtilInternal;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.util.EventUtil;

import ext.Action;
import ext.ComponentManager;
import ext.Ext;
import ext.StringUtil;
import ext.window.Window;

/**
 * Creates new content from a connector item selection.
 * An additional check is performed which verifies that the content
 * not already exists.
 */
public class CreateConnectorContentActionBase extends Action {

  [Bindable]
  public var selectedItemsValueExpression:ValueExpression;

  /**
   * @param config
   */
  public function CreateConnectorContentActionBase(config:CreateConnectorContentAction = null) {
    super(Action(Ext.apply({handler: runDuplicateCheck}, config)));
    setDisabled(true);
    config.selectedItemsValueExpression.addChangeListener(selectionChanged);
  }

  private function selectionChanged(ve:ValueExpression):void {
    setDisabled(true);
    var selection:Array = ve.getValue();
    if (!selection) {
      return;
    }

    for each(var selected:ConnectorObject in selection) {
      if (selected is ConnectorItem) {
        var entity:ConnectorItem = selected as ConnectorItem;
        if (entity.getTargetContentType()) {
          setDisabled(false);
          return;
        }
      }
    }
  }

  private function runDuplicateCheck():void {
    var selection:Array = initialConfig.selectedItemsValueExpression.getValue();
    if (selection && selection.length > 0) {
      var item:ConnectorItem = selection[0] as ConnectorItem;
      if (item) {
        var site:Site = editorContext.getSitesService().getPreferredSite();
        ConnectorContentService.findContent(item, null, site, function (c:Content):void {
          if (c) {
            var msg:String = StringUtil.format(resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_duplicate_exists_message'), c.getName());
            MessageBoxUtilInternal.show(resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'duplicate_title'), msg, null, {
                      yes: resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_duplicate_continue_btn_text'),
                      no: resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'create_duplicate_show_btn_text'),
                      cancel: resourceManager.getString('com.coremedia.cms.editor.Editor', 'btn_cancel')
                    },
                    function (btn:String):void {
                      if (btn === 'cancel') {
                        return;
                      }

                      var createContentAgain:Boolean = (btn === 'yes');
                      if (createContentAgain) {
                        createContentFor(item);
                      }
                      else {
                        editorContext.getCollectionViewManager().showInRepository(c);
                        EventUtil.invokeLater(function ():void {
                          editorContext.getCollectionViewManager().showInRepository(c);
                        });
                      }
                    });
          }
          else {
            createContentFor(item);
          }
        });
      }
    }
  }

  private function createContentFor(item:ConnectorItem):void {
    var itemsVE:ValueExpression = initialConfig.selectedItemsValueExpression;
    var onSuccess:Function = ContentCreationHelper.postProcessExternalContent;
    var processingDataFactory:Function = ContentCreationHelper.getProcessingDataFactory(itemsVE);
    var defaultNameExpression:ValueExpression = ContentCreationHelper.getDefaultNameExpression(itemsVE);
    var contentTypeExpression:ValueExpression = ContentCreationHelper.getContentTypeExpression(itemsVE);

    var config:Object = {
      contentTypeExpression: contentTypeExpression,
      skipInitializers: false,
      onSuccess: onSuccess,
      processingDataFactory: processingDataFactory,
      defaultNameExpression: defaultNameExpression
    };

    var qcdConfig:QuickCreateDialog = QuickCreateDialog(Ext.apply({}, config));
    var dialog:Window = ComponentManager.create(qcdConfig, 'window') as Window;
    dialog.show();
  }
}
}
