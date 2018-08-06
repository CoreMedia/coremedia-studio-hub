# Studio Hub Configuration

The configuration of the connector extension consists of different settings documents that can be
put in a global, site-specific or user's home folder.
The CoreMedia Blueprint comes with the following predefined settings documents:


## Connector Types

The document declares the list of available connector types. It defines the basic configuration that
all instances of these types are initialized with. New types can only be declared in this global settings file,
located in _/Settings/Options/Settings/Connector_.
Site specific declarations are currently not supported.
For new connector types, make sure that you configure the type also
in the _ConnectorTypes.properties_ file of the Studio.

The following properties are available for all connector types:

| Property | Mandatory | Description |
| -------- |:---------:| ----------- |
| name | no | The name of the root node shown in the Studio library. If not defined the fallback will be used as described in section 'Studio Customization'. |
| type | yes | The type name of the connector, this will define the type of a connection. |
| itemTypes | no | The _Connector Item Types_ document or empty if each connector item has it's own hard-coded type. |
| previewTemplates | no | The _Preview Templates_ document or empty, when each connector item renders it's own preview HTML. |
| contentMapping | yes | The _Content Mapping_ document for the connector item types. |
| invalidationInterval | no | Interval in seconds used to check if the items of a connector are still up to date. |
| notificationGroups | no | A list of comma separated values identifying the user groups to send notifications too. |
| notificationUsers | no | A list of comma separated values identifying the user names to send notifications too. |

## Connector Item Types

This document maps file endings or external system item types to a connector item type.
It is linked to the connector type definition inside the _Connector Types_ document.
Connector item types are similar to mime-types and help to determine the type
of preview to show for the current selection and to determine which CoreMedia content to create for it.
Connector item types also support subtypes like _text/html_ or _text/css_.
The extension can use the mediatype and subtype to specify more detailly how the preview
of the item should be generated. By default, implementations of the _ConnectorItem_ interface
will return a value of this configuration. Therefore, an empty 'default' value should always be
provided in this configuration in case a file type has not been mapped yet. Alternatively, a _ConnectorItem_
implementation can return it's own type.

## Content Mapping

The _Content Mapping_ document maps the defined connector item types to a CoreMedia content type.
It is linked to the connector type definition inside the _Connector Types_ document.

When a content is created out of an connector item, this list is used to determine the type. If the given
connector item type is not found in this list, the _default_ content type will be used.
Note that the item type of a connector must not be necessarily declared in
the _Connector Item Types_ document, but can be returned from the implementation
of the _ConnectorItem_ interface too (see _Connector Item Types_).

## Content Upload Types

The _Content Upload Types_ document describes which CoreMedia document types are allowed for uploading
and what are the blob property names of them. By default, the content upload is enabled when a category is writeable
or _isContentUploadEnabled_ returns true. In that case this settings document is used to apply the DnD logic for the Studio.
By default, the _upload_ method of the connector category is used to upload content blobs to the specific target system.

## Preview Templates

The document contains the HTML template that is used to display the preview inside the preview panel.
It is linked to the connector type definition inside the _Connector Types_ document.

The following XML shows the default "Preview Templates" document that should be located under _/Settings/Options/Settings/Connectors_.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<CMSettings folder="/Settings/Options/Settings/Connectors" name="Preview Templates"
            xmlns:cmexport="http://www.coremedia.com/2012/cmexport">
  <locale></locale>
  <master>
  </master>
  <settings>
    <Struct xmlns="http://www.coremedia.com/2008/struct" xmlns:xlink="http://www.w3.org/1999/xlink">
      <StringProperty Name="audio">&lt;audio controls&gt;&lt;source src=&quot;{0}&quot;&gt;Your browser does not support the audio tag.&lt;/audio&gt;</StringProperty>
      <StringProperty Name="video">&lt;video style="max-width:100%;" src=&quot;{0}&quot; controls&gt;&lt;/video&gt;</StringProperty>
      <StringProperty Name="picture">&lt;img style="max-width:100%;" src=&quot;{0}&quot; /&gt;</StringProperty>
      <StringProperty Name="text">&lt;textarea style=&quot;width:100%;overflow-x: scroll;resize: none;white-space: pre;overflow-wrap: normal;&quot; readonly rows=&quot;16&quot; wrap=&quot;soft&quot;&gt;{0}&lt;/textarea&gt;</StringProperty>
      <StringProperty Name="pdf">&lt;embed style=&quot;width:100%;min-height:300px;&quot; src=&quot;quot;{0}&quot; alt=&quot;quot;pdf&quot;quot; pluginspage=&quot;quot;http://www.adobe.com/products/acrobat/readstep2.html&quot;quot;&gt;</StringProperty>
      <StringProperty Name="youtube">&lt;iframe src=&quot;//www.youtube.com/embed/{0}&quot; class=&quot;cm-video cm-video--youtube&quot; frameborder=&quot;0&quot;
        style=&quot;width:100%;min-height:260px;&quot; webkitAllowFullScreen=&quot;&quot; allowFullScreen=&quot;&quot;&gt;&lt;/iframe&gt;</StringProperty>
    </Struct>
  </settings>
  <identifier></identifier>
</CMSettings>
```

This settings document determines the HTML that is used when the preview for a connector item is shown.
When an item is selected, the type of the icon is determined through it's name (see _Connector Item Types_)
or by the hard coded type value inside the implementation of the corresponding _ConnectorItem_ class.

An item's preview may be converted using a _PreviewConverter_. They can be used to convert binary data
into a human readable format, e.g. converting Word documents to HTML.

Additional metadata that has been extracted during this conversion process can be applied to the _PreviewConversionResult_ too.
If no custom preview is specified, a lookup is made in the template settings and the placeholder will be replaced
with the streaming URL of the item.

### Custom Preview Templates

It's obvious that for some cases the preview configuration with fix template definitions is not sufficient.
E.g. for the RSS connector, the preview can contain several images and additional text.
In that case, the default implementation of the _getPreviewHtml()_ of the _ConnectorItem_ interface
should be overwritten and return custom HTML.

## Connections

The document defines the instances of the connector types.
Each entry contains the actual connection credentials (if required) and
additional configuration attributes.
The _Connections_ content may be declared global in
folder _/Settings/Options/Settings/Connector_,
site-specific in folder _Options/Settings/_ or in the users home directory.
This way, it is possible that every user can integrate his or
hers own Dropbox folder/app or S3 bucket.

Every connection can/must configure the following common properties:

| Property  | Mandatory | Description |
| ----------| --------- | ------------- |
| displayName      | false | the name that is used for the connections tree node |
| connectionId   | true | the global unique id of the connection |
| type | true | the type of the connection as defined in _Connector Types_ |
| contentScope | false | the scope to use when duplicate content is checked during content creation |
| dateFormat | false | 'long' or 'short', defaults to 'long' which includes the time |
| previewThresholdMB | false | the max. amount of MB that is allowed to load for rendering it's preview |
| enabled | false | set to false to disable and hide this connection |
| rootNodeVisible | false | set to false if the connector tree should appear on the top level without an aggregator node |
| contentBlacklist | false | the blacklist of contents to be dropped if content drop is enabled for categories |
| contentWhitelist | false | the whitelist of contents to be dropped if content drop is enabled for categories |

The following sections explain the configuration of connections in detail.



### File System Configuration

The file system connector can be used to integrate a system folder that is located on the server side of the studio.
This can be a samba share or any other mounted filesystem. 

#### Sample Connection Struct

```xml
<Struct>
  <StringProperty Name="displayName">My Temp</StringProperty>
  <StringProperty Name="connectionId">tempFolder</StringProperty>
  <StringProperty Name="type">filesystem</StringProperty>
  <StringProperty Name="folder">/tmp</StringProperty>
  <IntProperty Name="previewThresholdMB">10</IntProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
</Struct>
```

#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| folder | the server side folder to embed in the Studio library |



### S3 Configuration

The S3 connector allows to show the content of Amazon S3 buckets in the Studio library.

#### Sample Connection Struct

```xml
<Struct>
  <StringProperty Name="displayName">My S3 Bucket</StringProperty>
  <StringProperty Name="connectionId">myS3Bucket</StringProperty>
  <StringProperty Name="type">s3</StringProperty>
  <StringProperty Name="accessKeyId">enter accessKey here</StringProperty>
  <StringProperty Name="secretAccessKey">enter secretAccessKey here</StringProperty>
  <StringProperty Name="region">eu-central-1</StringProperty>
  <StringProperty Name="bucketName">enter bucket name here</StringProperty>
  <IntProperty Name="previewThresholdMB">40</IntProperty>
</Struct>
```

#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| accessKeyId | the accessKey value used for authentication |
| secretAccessKey |the secretAccessKey used for authentication |
| region | the region the bucket is located in |
| bucketName | the name of the S3 bucket |
| proxyHost | the (optional) proxy host, only applied if proxy port and type is set too |
| proxyPort | the (optional) proxy port |
| proxyType | the (optional) proxy type (HTTP, HTTPS) |



### Dropbox Configuration

The Dropbox connector allows to connect to a Dropbox account with full access or a Dropbox app which access limited
to a specific folder. Visit https://www.dropbox.com/developers for details.
The 'Open In Management System' toolbar action will show the selected item in a new browser tab using
the integrated Dropbox preview.

#### Sample Connection Struct

```xml
<Struct>
  <StringProperty Name="displayName">Dropbox Folder</StringProperty>
  <StringProperty Name="connectionId">dropbox1</StringProperty>
  <StringProperty Name="type">dropbox</StringProperty>
  <StringProperty Name="accessToken">the access token of the app</StringProperty>
  <StringProperty Name="appName">the name of the app configured on dropox.com</StringProperty>
  <IntProperty Name="previewThresholdMB">40</IntProperty>
  <IntProperty Name="invalidationInterval">60</IntProperty>
  <StringProperty Name="notificationGroups">administratoren</StringProperty>
</Struct>
```

#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| accessToken | the accessToken value used for authentication |
| appName | the (optional) app name if an app is accessed and not the full Dropbox |
| proxyHost | the (optional) proxy host, only applied if proxy port and type is set too |
| proxyPort | the (optional) proxy port |
| proxyType | the (optional) proxy type (HTTP, HTTPS, FTP) |


### Cloudinary Configuration

The Cloudinary is used to access assets from the cloud service 'Cloudinary'.

#### Sample Connection Struct

```xml
<Struct>
  <StringProperty Name="displayName">Cloudinary</StringProperty>
  <StringProperty Name="connectionId">cloudinary1</StringProperty>
  <StringProperty Name="type">cloudinary</StringProperty>
  <StringProperty Name="cloudName">YOUR CLOUD NAME</StringProperty>
  <StringProperty Name="apiKey"></StringProperty>
  <StringProperty Name="apiSecret"></StringProperty>
  <StringProperty Name="contentScope">site</StringProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
</Struct>
```


#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| cloudName | the name of your cloud |
| apiKey | the API key value |
| apiSecret| the API secret value |


### CoreMedia Configuration

The CoreMedia connector can be used to copy documents between different CoreMedia repository.
It also supports a connection to an MLS to see what content is actually published.
The way content is copied my be customized by additional _ContentWriteInterceptor_ implementations.

#### Sample Connection Struct

```xml
<Struct>
  <StringProperty Name="displayName">CoreMedia Dev</StringProperty>
  <StringProperty Name="connectionId">coremedia1</StringProperty>
  <StringProperty Name="type">coremedia</StringProperty>
  <StringProperty Name="username"></StringProperty>
  <StringProperty Name="password"></StringProperty>
  <StringProperty Name="mediaType">CMMedia</StringProperty>
  <StringProperty Name="ignoredTypes">CMFolderProperties,Dictionary,Preferences,Query,CMSymbol,CMTheme,CMTemplateSet,CMSite,EditorPreferences</StringProperty>
  <StringProperty Name="path">/Sites</StringProperty>
  <StringProperty Name="ior">http://YOUR_SYSTEM:40180/coremedia/ior</StringProperty>
  <StringProperty Name="contentScope">site</StringProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
</Struct>
```


#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| username | the CAP user to be used to connector to the CoreMedia repository |
| password | the CAP user's password |
| ignoredTypes | document types that should not be shown in the list or search view |
| mediaType | the base document type used for previews, assuming the data is stored in the 'data' blob property |
| path | the (optional) path of the root node used when the CoreMedia content tree is shown |
| ior | the CoreMedia repository IOR url |


### Navigation Configuration

The Navigation connector shows the navigation tree for the preferred site and the child items of every selected node.

#### Sample Connection Struct

```xml
<Struct>
  <StringProperty Name="displayName">Navigation</StringProperty>
  <StringProperty Name="connectionId">navigation1</StringProperty>
  <StringProperty Name="type">navigation</StringProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
  <BooleanProperty Name="rootNodeVisible">false</BooleanProperty>
  <StringProperty Name="previewUrlPattern">http://preview.labsdev-tomcat-0-cms.coremedia.vm/blueprint/servlet/preview?id={0}</StringProperty>
  <StringProperty Name="contentTypeBlacklist"></StringProperty>
  <StringProperty Name="contentTypeWhitelist">CMTeasable</StringProperty>
</Struct>
```

#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| previewUrlPattern | The URL pattern used to generate the preview for the selected navigation item |


### RSS Feed Configuration

The RSS connector can be used to display the feed items of an RSS feed inside the Studio library.
Each feed entry's article can be opened by using the corresponding toolbar button 'Open In Tab'.

#### Sample Connection Struct 

```xml
<Struct>
  <StringProperty Name="displayName">Wired</StringProperty>
  <StringProperty Name="connectionId">wired</StringProperty>
  <StringProperty Name="type">rss</StringProperty>
  <StringProperty Name="url">http://www.wired.co.uk/rss</StringProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
  <IntProperty Name="invalidationInterval">60</IntProperty>
  <StringProperty Name="notificationGroups">administratoren</StringProperty>
</Struct>
```

#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| url | the RSS feed URL |
| proxyHost | the (optional) proxy host, only applied if proxy port and type is set too |
| proxyPort | the (optional) proxy port |
| proxyType | the (optional) proxy type (HTTP, HTTPS, FTP) |




### YouTube Configuration

Similar to the RSS connector, the YouTube connector shows the list of videos
of a YouTube account, including the defined playlists which are shown as sub-categories of the connector.
The preview integration of the Studio Hub allows to preview the video in the Studio library.
Ensure that http://www.youtube.com is that as valid iframe source in the _application.properties_ for
property _studio.security.csp.frameSrc_.

#### Sample Connection Struct (Channels)

```xml
<Struct>
  <StringProperty Name="displayName">CoreMedia Youtube Channel</StringProperty>
  <StringProperty Name="type">youtube</StringProperty>
  <StringProperty Name="connectionId">youtubeChef</StringProperty>
  <StringProperty Name="credentialsJson">enter JSON here</StringProperty>
  <StringProperty Name="channelId">enter channel id here</StringProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
</Struct>
```

The videos to show can also be resolved by using the YouTube username instead of a _channelId_:

#### Sample Connection Struct (Users)
```xml
<Struct>
  <StringProperty Name="displayName">Example.com Youtube Channel</StringProperty>
  <StringProperty Name="type">youtube</StringProperty>
  <StringProperty Name="connectionId">youtubeUserChannel</StringProperty>
  <StringProperty Name="credentialsJson">enter JSON here</StringProperty>
  <StringProperty Name="user">youtube-username</StringProperty>
  <BooleanProperty Name="enabled">true</BooleanProperty>
</Struct>
```

#### Connection Specific Properties

| Property  | Description |
| ------------- | ------------- |
| credentialsJson | the credentials JSON that is used for authentication against youtube |
| channelId | the id of the channel that should be displayed |



## Supported Preview Types

The embedded preview of the Studio Hub comes with some pre-defined preview converters.
These converters are used the generate HTML out of binary files or formats/converts text files.

The following preview conversion are supported:
 * Office documents (Word, Excel, Powerpoint, including other open document standards)
 * Markup files


## Supported File Types

The File System, S3 and Dropbox connector are file based connectors. The list of provided file types can be customized
by adapting the _Connector Item Types_ document. By default, all three connectors share the same settings document for this.
