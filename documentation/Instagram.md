## Instagram module

The instagram submodule is a basic integration example to work with the PUBLIC api of Instgram. This very limited API 
allows to read the recent items of an users feed as well as comments for the items.
This simple API does not allow for posting or to address specific post from other users.  

 
## Configuration

To use this module, integrate the extension your CoreMedia Studio Hub extension and configure the necessary setting documents.

The configuration of the instagram connection follows the standard configuration pattern as described in [Studio Hub Configuration](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/Configuration.md).
 
Add an entry into the Settings-Document "Connectors" with the following entries (example)
```xml

     <Struct>
      <StringProperty Name="displayName">Your Instagram Profle name</StringProperty>
      <StringProperty Name="connectionId">instagramccbucket</StringProperty>
      <StringProperty Name="contentScope">site</StringProperty>
      <StringProperty Name="type">instagram</StringProperty>
      <StringProperty Name="access_token">your access token</StringProperty>
      <IntegerProperty Name="previewThresholdMB">10</IntegerProperty>
      <BooleanProperty Name="enabled">true</BooleanProperty>
    </Struct>
```

In Settings Document "Content Mapping" add the entry:

```xml
  <StringProperty Name="Instagram">CMHTML</StringProperty>
```

In Settings Document "Content Types" add the entry:
```xml

     <Struct>
      <StringProperty Name="name">Instagram</StringProperty>
      <StringProperty Name="connectorType">instagram</StringProperty>
      <StringProperty Name="defaultColumns">type,name,createdTime</StringProperty>
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Connector Item Types" />
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Preview Templates" />
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Content Mapping" />
    </Struct>

```


