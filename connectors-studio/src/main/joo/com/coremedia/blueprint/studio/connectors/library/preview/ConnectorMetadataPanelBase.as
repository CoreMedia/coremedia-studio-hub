package com.coremedia.blueprint.studio.connectors.library.preview {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.cms.editor.sdk.premular.CollapsiblePanel;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.mixins.OverflowBehaviour;

import ext.StringUtil;
import ext.form.field.DisplayField;

public class ConnectorMetadataPanelBase extends CollapsiblePanel {

  [Bindable]
  public var selectedItemValueExpression:ValueExpression;

  [Bindable]
  public var metadataChangedExpression:ValueExpression;

  private var fields:Array = [];

  public function ConnectorMetadataPanelBase(config:ConnectorMetadataPanel = null) {
    super(config);
    config.metadataChangedExpression.addChangeListener(selectionChanged);
  }

  private function selectionChanged(ve:ValueExpression):void {
    for each(var field:DisplayField in fields) {
      remove(field);
    }
    updateLayout();

    var metadata:Object = ve.getValue();
    if (metadata) {
      for (var entry:String in metadata) {
        var label:String = entry;
        var formattedLabel:String = resourceManager.getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'metadata_' + label);
        if (formattedLabel) {
          label = formattedLabel;
        }
        else {
          label = ConnectorHelper.camelizeWithWhitespace(label);
        }

        var value:Object = metadata[entry];
        if(!value) {
          continue;
        }

        if (value is Date) {
          value = ConnectorHelper.formatDate(selectedItemValueExpression.getValue(), value);
        }
        else if(value is String) {
          var valueString:String = value as String;
          if(valueString.indexOf("http") === 0){
            var urlLabel:String = valueString;
            if(StringUtil.endsWith(urlLabel, '/')) {
              urlLabel = urlLabel.substr(0, urlLabel.length-1);
            }

            if(urlLabel.indexOf("/") !== -1) {
              urlLabel = urlLabel.substr(urlLabel.lastIndexOf('/')+1, urlLabel.length);
            }
           value = '<a href="' + value + '" target="_blank" style="overflow:hidden;color:black;" alt="' + valueString + '">' +  urlLabel + '</a>';
          }

          if(value.length === 0) {
            continue;
          }
        }

        addMetadata(label, value);
      }
    }
  }

  protected function formatDate(date:Object):String {
    return ConnectorHelper.formatDate(selectedItemValueExpression.getValue(), date);
  }

  private function addMetadata(label:String, value:Object):void {
    var field:DisplayField = new DisplayField(DisplayField({
      fieldLabel: label,
      value: value,
      labelSeparator: ':',
      labelAlign: 'left',
      overflowBehaviour : OverflowBehaviour.ELLIPSIS
    }));
    fields.push(field);

    add(field);
    updateLayout();
  }


  override protected function onDestroy():void {
    super.onDestroy();
    metadataChangedExpression.removeChangeListener(selectionChanged);
  }
}
}