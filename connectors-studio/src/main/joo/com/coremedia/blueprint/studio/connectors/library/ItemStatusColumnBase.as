package com.coremedia.blueprint.studio.connectors.library {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorItem;
import com.coremedia.ui.bem.IconWithTextBEMEntities;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.models.bem.BEMModifier;
import com.coremedia.ui.store.BeanRecord;

import ext.XTemplate;
import ext.data.Model;
import ext.data.Store;
import ext.grid.column.Column;
import ext.view.DataView;

[PublicApi]
public class ItemStatusColumnBase extends Column {

  /**
   * (optional) The modifier(s) to be used.
   * Multitype: can be String, Array or {@link BEMModifier}
   */
  [Bindable]
  public var modifier:*;

  /**
   * Defines if only the icon is to be displayed or if theres space for the text
   */
  [Bindable]
  public var iconOnly:Boolean = true;

  /**
   * The icon css class to use.
   */
  [Bindable]
  public var iconCls:String;

  /**
   * An additional text describing the icon. Will only be shown if {@link #iconOnly} is true.
   */
  [Bindable]
  public var iconText:String;

  /**
   * A tooltip to display when hoving the column.
   */
  [Bindable]
  public var toolTipText:String;

  public function ItemStatusColumnBase(config:ItemStatusColumn = null) {
    super(config);
  }

  //noinspection JSUnusedLocalSymbols
  /** @private */
  protected function getRenderer(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store, view:DataView):String {
    return this.tpl.apply({
      modifiers: getModifierCls(calculateModifier(value, metadata, record, rowIndex, colIndex, store)),
      iconCls: calculateIconCls(value, metadata, record, rowIndex, colIndex, store) || "",
      iconText: calculateIconText(value, metadata, record, rowIndex, colIndex, store) || "",
      toolTipText: calculateToolTipText(value, metadata, record, rowIndex, colIndex, store) || ""
    });
  }

  /** @private */
  protected static function getXTemplate():XTemplate {
    var xTemplate:XTemplate = new XTemplate([
      '<div aria-label="{iconText:escape}" class="' + IconWithTextBEMEntities.BLOCK + ' {modifiers:escape}" {toolTipText:unsafeQtip}>',
      '<span class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' {iconCls:escape}"></span>',
      '<span style="width: 0px;position:absolute;overflow:hidden;">{iconText:escape}</span>',
      '<span class="' + IconWithTextBEMEntities.ELEMENT_TEXT + '">{iconText:escape}</span>',
      '</div>'
    ]);
    return xTemplate;
  }

  /**
   * Used when the column should be used as button.
   * This template ensures that the button is accessible.
   */
  /** @private */
  protected static function getXButtonTemplate():XTemplate {
    var xTemplate:XTemplate = new XTemplate([
      '<div role="button" aria-label="{iconText:escape}" class="' + IconWithTextBEMEntities.BLOCK + ' {modifiers:escape}" {toolTipText:unsafeQtip}>',
      '<span class="' + IconWithTextBEMEntities.ELEMENT_ICON + ' {iconCls:escape}"></span>',
      '<span style="width: 0px;position:absolute;overflow:hidden;">{iconText:escape}</span>',
      '</div>'
    ]);
    return xTemplate;
  }

  /** @private */
  protected function getModifierCls(modifier:*):String {
    var classes:Array = [];
    iconOnly && classes.push(IconWithTextBEMEntities.MODIFIER_ICON_ONLY.getCSSClass());
    if (modifier) {
      var modifiers:Array = modifier as Array || [modifier];
      modifiers.forEach(function (modifier:*):void {
        if (modifier is String) {
          classes.push(IconWithTextBEMEntities.BLOCK.createModifier(modifier));
        }
        if (modifier is BEMModifier) {
          classes.push(modifier);
        }
      });
    }
    return classes.join(" ");
  }

  //noinspection JSUnusedLocalSymbols
  /** @private */
  protected function calculateModifier(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store):String {
    return modifier;
  }

  //noinspection JSUnusedLocalSymbols
  /** @private */
  protected function calculateIconCls(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store):String {
    var beanRecord:BeanRecord = record as BeanRecord;
    var bean:RemoteBean = beanRecord.getBean() as RemoteBean;
    if (!bean.isLoaded()) {
      return undefined;
    }

    var iconCls:String = null;
    if (bean is ConnectorItem) {
      var status:String = (bean as ConnectorItem).getStatus();
      if (status) {
        var key:String = "item_status_" + status;
        iconCls = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', key);
      }
    }
    return iconCls;
  }

  //noinspection JSUnusedLocalSymbols
  /** @private */
  protected function calculateIconText(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store):String {
    return iconText;
  }

  //noinspection JSUnusedLocalSymbols
  /** @private */
  protected function calculateToolTipText(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store):String {
    var beanRecord:BeanRecord = record as BeanRecord;
    var bean:RemoteBean = beanRecord.getBean() as RemoteBean;
    if (!bean.isLoaded()) {
      return undefined;
    }

    var tooltip:String = null;
    if (bean is ConnectorItem) {
      tooltip = (bean as ConnectorItem).getStatus();
    }

    if(tooltip) {
      tooltip = ConnectorHelper.camelizeWithWhitespace(tooltip);
    }

    return tooltip;
  }
}
}
