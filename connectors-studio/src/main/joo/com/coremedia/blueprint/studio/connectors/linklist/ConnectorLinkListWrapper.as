package com.coremedia.blueprint.studio.connectors.linklist {
import com.coremedia.blueprint.studio.connectors.helper.ConnectorHelper;
import com.coremedia.blueprint.studio.connectors.model.ConnectorEntity;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.cms.editor.sdk.util.LinkListWrapperBase;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.BeanState;
import com.coremedia.ui.data.PropertyChangeEvent;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.logging.Logger;

public class ConnectorLinkListWrapper extends LinkListWrapperBase {

  [Bindable]
  public var bindTo:ValueExpression;

  [Bindable]
  public var propertyName:String;

  [Bindable]
  public var linkTypeNames:Array;

  [Bindable]
  public var maxCardinality:int;

  [Bindable]
  public var model:Bean;

  [Bindable]
  public var createStructFunction:Function;

  [Bindable]
  public var readOnlyVE:ValueExpression;

  private var linksVE:ValueExpression;
  private var propertyExpression:ValueExpression;
  private var connectorObjRemoteBean:RemoteBean;

  public function ConnectorLinkListWrapper(config:ConnectorLinkListWrapper = null) {
    bindTo = config.bindTo;
    propertyName = config.propertyName;
    linkTypeNames = config.linkTypeNames;
    maxCardinality = config.maxCardinality || 0;
    model = config.model;
    createStructFunction = config.createStructFunction;
    readOnlyVE = config.readOnlyVE;
  }

  override public function getVE():ValueExpression {
    if (!linksVE) {
      linksVE = ValueExpressionFactory.createTransformingValueExpression(getPropertyExpression(),
              transformer, reverseTransformer, []);
    }
    return linksVE;
  }

  private function invalidateIssues(event:PropertyChangeEvent):void {
    if (event.newState === BeanState.NON_EXISTENT || event.oldState === BeanState.NON_EXISTENT) {
      var content:Content = bindTo.getValue() as Content;
      if (content && content.getIssues()) {
        content.getIssues().invalidate();
      }
    }
  }

  protected function getPropertyExpression():ValueExpression {
    if (!propertyExpression) {
      if (bindTo) {
        if (bindTo.getValue() is Content) {
          propertyExpression = bindTo.extendBy('properties').extendBy(propertyName);
        } else {
          propertyExpression = bindTo.extendBy(propertyName);
        }
      } else {
        propertyExpression = ValueExpressionFactory.create(propertyName, model);
      }
    }
    return propertyExpression;
  }

  override public function getTotalCapacity():int {
    return maxCardinality > 0 ? maxCardinality : int.MAX_VALUE;
  }

  override public function getFreeCapacity():int {
    if (!maxCardinality) {
      return int.MAX_VALUE;
    }
    //noinspection JSMismatchedCollectionQueryUpdate
    var connectorItems:Array = getVE().getValue() as Array;
    return maxCardinality - connectorItems.length;
  }

  override public function acceptsLinks(links:Array):Boolean {
    var targetSiteId:String = getTargetSiteId();
    return links.every(function(link:Object):Boolean {
      var connectorObject:ConnectorEntity = getConnectorObject(link);
      if (!connectorObject) {
        return false;
      }

      return true;
    });
  }

  /**
   * Return the site of the currently bound content or the preferred site.
   * May be undefined.
   */
  private function getTargetSite():Site {
    if (bindTo) {
      var content:Content = bindTo.getValue() as Content;
      if (content) {
        return editorContext.getSitesService().getSiteFor(content);
      }
    }
    //no content there. so let's take the preferred site
    return editorContext.getSitesService().getPreferredSite();
  }

  /**
   * Return the the target site id. May be undefined.
   */
  private function getTargetSiteId():String {
    var site:Site = getTargetSite();
    return site && site.getId();
  }

  private static function getConnectorObject(link:Object):ConnectorEntity {
    var connectorObject:ConnectorEntity = link as ConnectorEntity;
    return connectorObject;
  }

  override public function getLinks():Array {
    return getVE().getValue();
  }

  override public function setLinks(links:Array):void {
    if (createStructFunction) {
      createStructFunction.apply();
    }
    var myLinks:Array = links.map(getConnectorObject);
    //are some links yet not loaded?
    //noinspection JSMismatchedCollectionQueryUpdate
    var notLoadedLinks:Array = myLinks.filter(function(myLink:ConnectorEntity):Boolean {
      return !myLink.isLoaded();
    });
    if (!notLoadedLinks || notLoadedLinks.length === 0) {
      getVE().setValue(myLinks);
    } else {
      notLoadedLinks.every(function(notLoadedLink:ConnectorEntity):void {
        notLoadedLink.load(function():void {
          setLinks(myLinks);
        });
      });
    }
  }

  override public function isReadOnly():Boolean {
    return readOnlyVE ? readOnlyVE.getValue() : false;
  }

  private function transformer(value:*):Array {
    var valuesArray:Array = [];
    if (value) {
      //the value can be a string or a connector object bean
      if (value is String || value is ConnectorEntity) {
        //this is a single connector object stored
        valuesArray = [value];
      } else if (value is Array) {
        //this are multiple connector objects stored in an array
        valuesArray = value;
      }
    }

    return valuesArray.map(function (value:*):ConnectorEntity {
      //the value can be a string or a connector object bean
      var connectorObject:ConnectorEntity;

      if (value is ConnectorEntity) {
        connectorObject = value;
      } else if (value is String) {
        connectorObject = ConnectorHelper.getConnectorObject(value) as ConnectorEntity;
      } else {
        Logger.error("ConnectorLink does not accept the value: " + value);
      }

      if (connectorObject === undefined) {
        return undefined;
      }

      if (connectorObject !== connectorObjRemoteBean) {
        if (connectorObjRemoteBean) {
          connectorObjRemoteBean.removePropertyChangeListener(BeanState.PROPERTY_NAME, invalidateIssues);
        }
        connectorObjRemoteBean = connectorObject;
        if (connectorObject) {
          connectorObjRemoteBean.addPropertyChangeListener(BeanState.PROPERTY_NAME, invalidateIssues);
        }
      }
      return connectorObject;
    });
  }

  private function reverseTransformer(value:Array):* {
    if (value && value.length > 0) {
      if (maxCardinality === 1) {
        return ConnectorEntity(value[0]).getConnectorId();
      } else {
        return value.map(function (bean:ConnectorEntity):String {
          return bean.getConnectorId();
        })
      }
    }
    return maxCardinality === 1 ? "" : [];
  }

}
}
