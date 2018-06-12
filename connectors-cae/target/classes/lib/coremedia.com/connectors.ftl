<#-- @ftlvariable name="connectorFreemarkerFacade" type="com.coremedia.blueprint.connectors.cae.web.taglib.ConnectorFreemarkerFacade" -->

<#-- Access the original entity that is linked inside the local settings of teaseable-->
<#function getItem teasable>
  <#return connectorFreemarkerFacade.getConnectorItem(teasable)>
</#function>

<#-- Access the entities that are linked as a link list in the local settings -->
<#function getItems teasable>
  <#return connectorFreemarkerFacade.getConnectorItems(teasable)>
</#function>