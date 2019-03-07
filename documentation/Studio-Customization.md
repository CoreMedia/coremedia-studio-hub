# Studio Customization

Developing new connectors requires client-side Studio customization too. These customization are easily done by overriding
the connector property files of the Connector Studio plugin. Examples for overriding property files
can be found in documentation or the file 'BlueprintFormsStudioPlugin.mxml' inside the CoreMedia Blueprint.

### Customizing Labels and Icons

The property file _ConnectorsStudioPlugin.properties_ contains the label and icon values
for connector categories and items. New entries can simply be added by overriding this file.
If a connector item type has a subtype, e.g. _text/html_, only the last segment is used
to determine the label or icon.

The file expects entries in the following format:

#### Connectors
```
connector_type_<CONNECTOR_TYPE>_name = <TYPE_LABEL>
connector_type_<CONNECTOR_TYPE>_icon = Resource(key='<KEY_FOR_ICON>', bundle='com.coremedia.icons.CoreIcons')
```
For icon values, we recommend to use the existing _CoreIcons_ resource.
If a _name_ property is set for the connector type inside the _Connector Types_ document, this _name_ property will be ignored.


#### Categories
```
category_type_<CONNECTOR_CATEGORY_TYPE>_name = <TYPE_LABEL>
category_type_<CONNECTOR_CATEGORY_TYPE>_icon = Resource(key='l<KEY_FOR_ICON>', bundle='com.coremedia.icons.CoreIcons')
```
For icon values, we recommend to use the existing _CoreIcons_ resource.

#### Items
```
item_type_<CONNECTOR_ITEM_TYPE>_name = <TYPE_LABEL>
item_type_<CONNECTOR_ITEM_TYPE>_icon = Resource(key='<KEY_FOR_ICON>', bundle='com.coremedia.icons.CoreIcons')
```
For icon values, we recommend to use the existing _CoreIcons_ resource.

#### Meta Data Labels
The keys of metadata are automatically formatted as camel-case with separated white spaces.
If a custom label should be applied for a metadata key, a value can be configured with the following format:
```
metadata_<KEY> = <VALUE>
```

### Custom Columns

The Content Hub allows to add custom columns to the Studio library and to configure the default ones.

#### Configuring Default Columns

For each connector type that loaded, the default columns are configurable.
For example, for the RSS connector, we don't want to display the _Size_ column. This can be achieved by setting the optional
_StringProperty_ _defaultColumns_ inside the _Connector Type_ definition:

```xml
<Struct>
  <StringProperty Name="name">RSS Feeds</StringProperty>
  <StringProperty Name="connectorType">rss</StringProperty>
  <StringProperty Name="defaultColumns">type,name,lastModified</StringProperty>
  ...
```

#### Adding Custom Columns

If a new column should be added for a connector, the corresponding _ConnectorCategory_ implementation of the connector
have to implement the method _getColumns()_ which returns the list of columns that should be display for this connector.
For example, the RSS connector declares an additional column _Author_:

```java
  public List<ConnectorColumn> getColumns() {
    return Arrays.asList(new DefaultConnectorColumn("author_header", "author", 120, 2));
  }
```

![Custom Columns](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/custom_columns.png)

The method used the default implementation _DefaultConnectorColumn_. The first parameter is the resource bundle
key that should be used from the Studio resource bundle _ConnectorsStudioPlugin.properties_.
The second parameter is the name of the data index that should be used when the data is displayed.
It must match with the data index given for a specific column value.
The width and index of the column is applied here too.
If the index is set to -1, the column will be added after after the _Name_ column.

The column is set up now. To apply data for it, each _ConnectorItem_ must implement the method _getColumnValues()_.
The given example is taken from the class _RssConnectorItem.java_:

```java
  public List<ConnectorColumnValue> getColumnValues() {
    return Arrays.asList(new DefaultConnectorColumnValue(rssEntry.getAuthor(), "author"));
  }
```

The example uses the default implementation for a column value _DefaultConnectorColumnValue.java_.
As a result, the author name of the RSS article will be shown in the "Author" column.
The default implementation allows to configure an icon for the column too:

```java
  public List<ConnectorColumnValue> getColumnValues() {
    return Arrays.asList(new DefaultConnectorColumnValue("publish, "author", "My Icon Text", "My Icon Tooltip"));
  }
```

If this constructor is used, the column value ("publish") will be looked up in the Studio resource bundle _CoreIcons.properties_
and the icon will be shown with it's tooltip and the given optional icon text.


### Custom Forms

The Content Hub comes with a custom link list that can be added to any document form of a content type, that owns a struct property.

![Custom Link Lists](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/content_ll.png)

To add a connector link list to a form, you have to use a regular _AddItemsPlugin_ inside your _StudioPlugin_. For example, when you add the following snippet to you _BlueprintFormsStudioPlugin.mxml_

```xml
<bpforms:MediaDocumentForm>
  <bpforms:plugins exml:mode="append">
    <ui:AddItemsPlugin>
      <ui:items>
        <connectors:ConnectorLinkListPropertyField itemId="connectorLinkList"
                                                   showThumbnails="true"
                                                   propertyName="localSettings.connectorIds"/>
      </ui:items>
    </ui:AddItemsPlugin>
  </bpforms:plugins>
</bpforms:MediaDocumentForm>
```

every form definition that includes the _MediaDocumentForm_ will also contain the link list _External Media Items_.
The label of this link list is localized like any other property editor, but by default, the Content Hub supports the label for the _Struct_ property _localSettings.connectorIds_, where the actual items are stored.

Note that you can not only drop _ConnectorItem_ entities on the link list, but also _ConntectorCategory_ entities. This allows you to render all children of a category without touching any CoreMedia content.

The _CAE_ modules of the Content Hub comes with a Freemarker fascade that allows to access the linked items from _ContentBeans_.
