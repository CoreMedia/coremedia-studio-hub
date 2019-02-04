package com.coremedia.blueprint.studio.connectors.push {
import com.coremedia.blueprint.studio.connectors.model.ConnectorCategory;
import com.coremedia.cms.editor.sdk.collectionview.tree.CompoundTreeModel;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.models.TreeModel;

import ext.Ext;
import ext.XTemplate;
import ext.data.NodeInterface;
import ext.tree.TreePanel;

public class ConnectionTreePanelBase extends TreePanel {
  [Bindable]
  public var rootCategory:ConnectorCategory;

  [Bindable]
  public var selectionExpression:ValueExpression;

  private var treeModel:TreeModel;

  protected static const CELL_TPL:XTemplate = new XTemplate(
          '{%',
          'this.addAriaAttrs(values);',
          '%}',
          '<td class="{tdCls}" {tdAttr} {cellAttr:attributes} {ariaAttrs:attributes} tabIndex="-1"',
          ' style="width:{column.cellWidth}px;<tpl if="tdStyle">{tdStyle}</tpl>"',
          '<tpl if="column.cellFocusable === false">',
          ' role="presentation"',
          '<tpl else>',
          ' role="{cellRole}" tabindex="-1"',
          '</tpl>',
          '  data-columnid="{[values.column.getItemId()]}">',
          '<div {unselectableAttr} class="' + Ext.baseCSSPrefix + 'grid-cell-inner {innerCls}" ',
          'style="text-align:{align};<tpl if="style">{style}</tpl>" ',
          '{cellInnerAttr:attributes}>{value}</div>',
          '</td>',
          {
            priority: 0,
            addAriaAttrs: function (values:Object):void {
              // Adding aria attributes to <td> nodes
              var record:NodeInterface = values.record;

              values.ariaAttrs = {};

              values.ariaAttrs['aria-level'] = record.getDepth();
              values.ariaAttrs['aria-expanded'] = record.isExpanded();
            }
          }
  );

  public function ConnectionTreePanelBase(config:ConnectionTreePanel = null) {
    super(config);
  }

  protected function getTreeModel(rootCategory:ConnectorCategory):TreeModel {
    if (!treeModel) {
      var compoundTreeModel:CompoundTreeModel = editorContext.getCollectionViewManager()['getLibraryTreeModel']();
      treeModel = compoundTreeModel.getTreeModel(rootCategory.getUriPath());
    }
    return treeModel;
  }
}
}
