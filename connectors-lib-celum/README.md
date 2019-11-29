# Celum Content Hub Connector

## Release Notes (17.07.2018)

- changed setting _celumApiKey_ to _apiKey_
- added configurable host name
- fixed connector for multiple Celum connections

## Installation

The Celum Content Hub Connector is based on the CoreMedia Content Hub.
For details about the installation and latest Content Hub updates, please check
the documentation (https://github.com/CoreMedia/coremedia-studio-hub/tree/master/documentation)
or the latest release notes for updates.

https://github.com/CoreMedia/coremedia-studio-hub/releases

## Configuration

The configuration of the Celum Connector is completely content based.
To configure a Celum connection, add the following configuration to the the
_/Options/Settings/Options/Connector/_ configuration documents.

_Connections_:

```
<Struct>
  <StringProperty Name="displayName">Celum Content Picker</StringProperty>
  <StringProperty Name="connectionId">celum1</StringProperty>
  <StringProperty Name="type">celum</StringProperty>
  <StringProperty Name="apiKey">API_KEY</StringProperty>
  <StringProperty Name="contentScope">site</StringProperty>
  <StringProperty Name="host">YOUR_CELUM_HOST</StringProperty>
  <IntProperty Name="defaultDownloadFormat">1</IntProperty>
  <StructProperty Name="downloadFormats">
    <Struct>
      <IntProperty Name="jpg,jpeg">16</IntProperty>
    </Struct>
  </StructProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
  <StringProperty Name="dateFormat">short</StringProperty>
  <StringProperty Name="managementUrl">https://dam.celum.com</StringProperty>
</Struct>
```

_Connector Types_:

```
<Struct>
  <StringProperty Name="name">Celum</StringProperty>
  <StringProperty Name="connectorType">celum</StringProperty>
  <LinkProperty Name="itemTypes" LinkType="coremedia:///cap/contenttype/CMSettings"
                xlink:href="coremedia:///cap/resources/Connector%20Item%20Types.xml"
                cmexport:path="/Settings/Options/Settings/Connectors/Connector%20Item%20Types"/>
  <LinkProperty Name="previewTemplates" LinkType="coremedia:///cap/contenttype/CMSettings"
                xlink:href="coremedia:///cap/resources/Preview%20Templates.xml"
                cmexport:path="/Settings/Options/Settings/Connectors/Preview%20Templates"/>
  <LinkProperty Name="contentMapping" LinkType="coremedia:///cap/contenttype/CMSettings"
                xlink:href="coremedia:///cap/resources/Content%20Mapping.xml"
                cmexport:path="/Settings/Options/Settings/Connectors/Content%20Mapping"/>
</Struct>
```

## Customization

#### Custom Fields

Since the set of values of the field 'informationFieldValues' may be customized,
the corresponding REST request for accessing asset details should be customized accordingly.

This can easily be done inside the method

```
CelumCoraService#expandAsset
```

where

```
queryParams.add("$expand", "informationFieldValues");
```

allows to configure the fields that should be expanded. The result will be stored in the field

```
Map<String,Object> informationFieldValues;
```

of the _Asset_ class and can be processed when a _ContentWriteInterceptor_ is executed for creating content out of Celum assets.
For a better access to these custom _informationFieldValues_, we recommend map the fields to concrete _InformationFieldValues_ class.
This class can re-use existing representation classes then, like _LocalizedLabel_ or _Tag_.


#### Content Item Write Interceptors

In order to use custom fields that have been configured for Celum,
a new _ContentWriteInterceptor_ has to be implemented which processes these fields.

A good example for custom a _ConnectorItemWriteInterceptor_ can be found in class _RssContentItemWriteInterceptor_.
The Celum asset instance can be accessed through the request properties:

```
CelumConnectorItem entity = (CelumConnectorItem) properties.get(CONNECTOR_ENTITY);
if (entity instanceof RssConnectorItem) {
   CelumConnectorItem celumAsset = (CelumConnectorItem) entity;
   celumAsset.expand(); //make sure all details are loaded
   ...
}
```

The _expand_ call ensures that all fields (type information and additional information fields) are loaded.
The asset object itself provided all data required for creating a CoreMedia content.
Also, the _stream_ method returns an _InputStream_ that can be used to create Coremedia blobs.



