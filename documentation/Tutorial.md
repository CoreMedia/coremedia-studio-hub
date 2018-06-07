# How to create a new Connector

In this tutorial, we will implement a connector for the Studio Hub.
The example connector will be implemented as a separate extension and does not connect to a specific system.
Instead, we simulate the Studio Hub categories and items programmatically.

The full example of this tutorial can be downloaded [here](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/downloads/studio-hub-example.zip "Studio Hub Example").

## Prerequisites

- make sure you have the latest CoreMedia Blueprint
- make sure that you have the Studio Hub extension installed: in binary or source format

The additional entry that will add the Studio Hub extension to the Blueprint should look like:

```
com.coremedia.blueprint.connectors:connectors:<LATEST VERSION>
```

Checkout the release page for the latest version of the Studio Hub:
https://github.com/CoreMedia/coremedia-studio-hub/releases



# Step 1 - Extension Setup

First of all we have to create a new extension with a _studio_ and _studio_ lib module.
The extension created in this tutorial is called _studio-hub-example_ and can
be downloaded [here](downloads/studio-hub-example-start.zip "Studio Hub Example Start").

 * Download the empty example modules and put it into the _extensions_ folder of your CoreMedia Blueprint.
 * Unpack the zip archive.
 * Link the new extension as _module_ into the parent _pom.xml_.
 * Make sure that the example extension has the correct group _groupId_ and _version_ in both _pom.xml_ files.
 * Add the example extension to your _workspace-configuration/managed-extensions.txt_ file.
 * Execute the extension tool to update your configuration poms for the Studio client and server libraries.

### Checkpoint

At this point, the following things should have happened:

 * The file _modules/extension-config/studio-extension-dependencies/pom.xml_ file contains the artifact _studio-hub-example_.
 * The file _modules/extension-config/studio-lib-extension-dependencies/pom.xml_ file contains the artifact _studio-hub-example_.
 * The rebuild Studio webapp is started with the additional plugin _Studio Hub Example_ (there won't be anything to see in the Studio library yet!).
 * IDEA has the modules detected properly and you see the following structure:

 
 ![Maven Modules](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/maven-modules.png)

# Step 2 - Implementing the Studio Module (Backend)

The empty example module already contains the classes that are required when implementing a new connector.
In this step, we implement the minimal set of methods so that we get visual feedback of the connector in the Studio.
This section shows you how to implement the Studio Hub classes to integrate you own external system.

### ConnectorService Implementation

The _ConnectorService_ implementation provides all functionality of the connector, starting from
providing the categories and items, to up- and downloading of assets.

#### Init and Shutdown

The _init_ method is invoked when a new connection is created. It is usually used
to create a connection to an external system.
If the method returns _false_ or throws an exception, the connection is not active
and won't be shown in the Studio library tree.


```java
@Override
public boolean init(@Nonnull ConnectorContext context) throws ConnectorException {
    this.context = context;
    LOG.info("Studio Hub example initialized.");
    return true;
}
```

The _shutdown_ method is invoked when a connection is invalidated. This occurs
when the settings of a connection are changed. In that case, the shutdown method
may be implemented to ensure a clean shutdown of existing connections.
Since we have no connection, we can also remove the method from the class
since an empty default implementation already exists in the interface.

#### Creating the Root Category

Categories are provided by implementing the method _getRootCategory_ and _getCategory_.
As a general rule, every model that is created here should be provided with the immediate parent
and child objects. So, a category should always know it's parent, child categories and items.

So let's start implementing the _getRootCategory_ method:

```java
@Nonnull
@Override
public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if(rootCategory == null) {
      String name = this.context.getProperty("displayName");
      List<ConnectorCategory> childCategories = Collections.emptyList();
      List<ConnectorItem> childItems = Collections.emptyList();
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new ExampleConnectorCategory(id, context, name, null, childCategories, childItems);
    }
    return rootCategory;
}
```

Since the root category is called from several other methods, we cache it as an object variable.
Also, we assume that the _StringProperty_ _displayName_ was configured in the connection settings for the connector.
All required parameters for a category are passed as constructor parameters, therefore we have
to adapt the _ExampleConnectorCategory_ to return the given values:

```java
public class ExampleConnectorCategory implements ConnectorCategory {
  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private ExampleConnectorCategory parent;
  private List<ConnectorCategory> childCategories;
  private List<ConnectorItem> childItems;

  public ExampleConnectorCategory(ConnectorId id,
                                  ConnectorContext context,
                                  String name,
                                  ExampleConnectorCategory parent,
                                  List<ConnectorCategory> childCategories,
                                  List<ConnectorItem> childItems) {
    this.id = id;
    this.context = context;
    this.name = name;
    this.parent = parent;
    this.childCategories = childCategories;
    this.childItems = childItems;
  }

  @Nonnull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return childCategories;
  }

  @Nonnull
  @Override
  public List<ConnectorItem> getItems() {
    return childItems;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @Override
  public Boolean isDeleteable() {
    return false;
  }

  @Override
  public Boolean delete() {
    return false;
  }
}
```

#### Creating the Child Categories

We now have a valid root category, but without any items or sub-categories.
The initial example shown above only returns an empty list for child categories.
For a concrete service implementation, we would have some kind of client API here
that would give us the child categories of the root category. In our example however,
we simply create two subcategories for the root category manually and store them as object variables:

```java
@Nonnull
@Override
public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if(rootCategory == null) {
      String name = this.context.getProperty("displayName");
      List<ConnectorCategory> childCategories = new ArrayList<>();
      List<ConnectorItem> childItems = Collections.emptyList();
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new ExampleConnectorCategory(id, context, name, null, childCategories, childItems);

      //create fixed child categories
      ConnectorId category1Id = ConnectorId.createCategoryId(context.getConnectionId(), "A");
      subCategory1 = new ExampleConnectorCategory(category1Id, context, "Sub A", rootCategory, Collections.emptyList(), Collections.emptyList());
      childCategories.add(subCategory1);

      ConnectorId category2Id = ConnectorId.createCategoryId(context.getConnectionId(), "B");
      subCategory2 = new ExampleConnectorCategory(category2Id, context, "Sub B", rootCategory, Collections.emptyList(), Collections.emptyList());
      childCategories.add(subCategory2);
    }
return rootCategory;
}
```

The implementation of the _getCategory_ method is simple now, since we already know all existing categories.
Once again: for a concrete service implementation we would use a client API here.

```java
@Nullable
@Override
public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    if(id.isRootId()) {
      return rootCategory;
    }

    if(id.equals(subCategory1.getConnectorId())) {
      return subCategory1;
    }

    if(id.equals(subCategory2.getConnectorId())) {
      return subCategory2;
    }
    return null;
}
```

#### Creating the Child Items

Connector items are leaves of connector categories. They represent the actual external content or asset that should be displayed
in the Studio. The _ConnectorItem_ interface looks similar to that of the _ConnectorCategory_.
Let's add two connector items to the first subcategory 'A'. For the simplicity of this example, these items are stored
as object variables again. Since we create all connector objects in the _getRootCategory_ method, we once again adapt the
implementation:

```java
  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if(rootCategory == null) {
      String name = this.context.getProperty("displayName");
      List<ConnectorCategory> childCategories = new ArrayList<>();
      List<ConnectorItem> childItems = Collections.emptyList();
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new ExampleConnectorCategory(id, context, name, null, childCategories, childItems);

      //create fixed child categories with fix children
      ConnectorId item1d = ConnectorId.createItemId(context.getConnectionId(), "item1");
      exampleItem1 = new ExampleConnectorItem(item1d, context, "Item 1", subCategory1);
      ConnectorId item2d = ConnectorId.createItemId(context.getConnectionId(), "item2");
      exampleItem2 = new ExampleConnectorItem(item2d, context, "Item 2", subCategory1);

      ConnectorId category1Id = ConnectorId.createCategoryId(context.getConnectionId(), "A");
      subCategory1 = new ExampleConnectorCategory(category1Id, context, "Sub A", rootCategory, Collections.emptyList(), Arrays.asList(exampleItem1, exampleItem2));
      childCategories.add(subCategory1);

      ConnectorId category2Id = ConnectorId.createCategoryId(context.getConnectionId(), "B");
      subCategory2 = new ExampleConnectorCategory(category2Id, context, "Sub B", rootCategory, Collections.emptyList(), Collections.emptyList());

      childCategories.add(subCategory2);
    }
    return rootCategory;
  }
```

The service method _getItem_ can then be implemented with a simple connector ID comparison:

```java
  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    if(id.equals(exampleItem1.getConnectorId())) {
      return exampleItem1;
    }

    if(id.equals(exampleItem2.getConnectorId())) {
      return exampleItem2;
    }
    return null;
  }
```

Finally, we create a constructor for the _ExampleConnectorItem_ and assign the parameters to the
corresponding getter methods:

```java
public class ExampleConnectorItem implements ConnectorItem {

  private ConnectorId id;
  private ConnectorContext context;
  private String name;
  private ConnectorCategory category;

  public ExampleConnectorItem(ConnectorId id, ConnectorContext context, String name, ConnectorCategory category) {
    this.id = id;
    this.context = context;
    this.name = name;
    this.category = category;
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Nullable
  @Override
  public String getDescription() {
    return null;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return null;
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return null;
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }

  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @Nonnull
  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Nullable
  @Override
  public ConnectorCategory getParent() {
    return category;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return null;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @Override
  public Boolean isDeleteable() {
    return false;
  }

  @Override
  public Boolean delete() {
    return false;
  }
}
```

We have finished the minimal amount of Java required to implement a new connector and can continue with
the Studio part in the next step. For that, rebuild the _studio-webapp_ with all dependencies and start the Studio webapp.

### Checkpoint

- The Studio webapp is up and running.
- The Studio webapp is accessible under the URL http://localhost:41080/blueprint/studio/studio-app/target/app/


## Step 3 - Connection Type Configuration

Additional content has to be configured to set up the new connector properly. First of all, we have to specify the type
details in the setting document _Connector Types_. You can find details about the linked configuration in the
section "Configuration". The example given here is the default used for file based connectors such as Dropbox or S3 buckets.

__We recommend to re-use the default configurations for _itemTypes_, _previewTemplates_, _contentUploadTypes_, and _contentMapping_ and store them in the
global folder _/Settings/Options/Settings_. The exported content files for these settings documents can be found [here](https://github.com/CoreMedia/coremedia-studio-hub/tree/master/test-data).__

The folder contains also additional example configurations for existing connectors.


```xml
<Struct xmlns="http://www.coremedia.com/2008/struct" xmlns:xlink="http://www.w3.org/1999/xlink">
  <StructListProperty Name="types">
    <Struct>
      <StringProperty Name="name">Example Connectors</StringProperty>
      <StringProperty Name="connectorType">example</StringProperty>
      <LinkProperty Name="itemTypes" LinkType="coremedia:///cap/contenttype/CMSettings"
                    xlink:href="coremedia:///cap/resources/Connector%20Item%20Types.xml"
                    cmexport:path="/Settings/Options/Settings/Connectors/Connector%20Item%20Types"/>
      <LinkProperty Name="previewTemplates" LinkType="coremedia:///cap/contenttype/CMSettings"
                    xlink:href="coremedia:///cap/resources/Preview%20Templates.xml"
                    cmexport:path="/Settings/Options/Settings/Connectors/Preview%20Templates"/>
      <LinkProperty Name="contentMapping" LinkType="coremedia:///cap/contenttype/CMSettings"
                    xlink:href="coremedia:///cap/resources/Content%20Mapping.xml"
                    cmexport:path="/Settings/Options/Settings/Connectors/Content%20Mapping"/>
      <LinkProperty Name="contentUploadTypes" LinkType="coremedia:///cap/contenttype/CMSettings"
                    xlink:href="coremedia:///cap/resources/Content%20Upload%20Types.xml"
                    cmexport:path="/Settings/Options/Settings/Connectors/Content%20Upload%20Types"/>
    </Struct>
    ...
```

### Checkpoint

- The given struct has been added to the document _Connector Types_ that is located in the global settings folder.




## Step 4 - Connection Instance Configuration

The type of a connector is now known to the Studio in backend and frontend. Finally we have to create
an instance of the new connector type in the _Connections_ settings document.
You can use the existing settings document and add your entry there or create a new one
and put it into a site specific folder or your home folder.


```xml
<Struct xmlns="http://www.coremedia.com/2008/struct" xmlns:xlink="http://www.w3.org/1999/xlink">
  <StructListProperty Name="connections">
     <Struct>
      <StringProperty Name="displayName">Example Connector</StringProperty>
      <StringProperty Name="connectionId">example1</StringProperty>
      <StringProperty Name="type">example</StringProperty>
      <StringProperty Name="contentScope">site</StringProperty>
      <BooleanProperty Name="enabled">true</BooleanProperty>
     </Struct>
     ...
```

Save the new settings struct and reload the Studio.

### Checkpoint

- The new tree should be visible in the Studio library now.

![Example Connector](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/tree1.png)

## Step 5 - Connector Item Types

As you can see in the screenshot above, the items have the type _Download_ which is the default content type when
no specific connector item type is detected.
Let's 'convert' our example items to images.
Since we have the linked the default _Connector Item Types_ and _Preview Templates_
we can use the name to let the Studio Hub determine the type of an item.
For example, initialize the first connector item in the service class with the name "test.png" to pretend that
is an image:

```java
ConnectorId item1d = ConnectorId.createItemId(context.getConnectionId(), "item1");
exampleItem1 = new ExampleConnectorItem(item1d, context, "test.png", subCategory1);
```

Once you rebuild and reload the Studio, the item will be recognized as an image.

![Image Connector Item](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/item1.png)

You see that the type is changed. Because the file ending '.png' is mapped as type _image_
in the _Connector Item Types_, the preview template will try to render an image.
The URL is still broken. We fix that by implementing the _stream_ method inside
the _ExampleConnectorItem_ class. The given example simply uses an image from the web to return something.
In a concrete implementation, the stream would access the corresponding external resource:

```java
  @Nullable
  @Override
  public InputStream stream() {
    try {
      URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1c/FuBK_testcard_vectorized.svg/2000px-FuBK_testcard_vectorized.svg.png");
      return url.openStream();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
```

![Image Connector Item with Preview](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/item2.png)

The preview panel shows the image now. The preview of other item types is generated in a similar way.
For example you can change the name of the item to "test.txt" and the preview
will try to render the content of a file that is read via the _stream_ method.

Finally, let's add some metadata to the item. This is simply achieved by
putting some values into the map that is returned by the _getMetadata_ method.

```java
  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    data.put("author", "it' me!");
    data.put("source", "Wikipedia");
    return () -> data;
  }
```

### Checkpoint

The item selection should look like this:

![Image Connector Item with Preview and Metadata](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/item3.png)


## Step 6: Custom Connector Item Types

While the existing type support is useful for simple file assets, there are
often items that have a custom type and require a custom preview.
Let's assume the second item is some kind of text from an external system.
First of all, rename the item to a more meaningful name in the _ExampleConnectorService_:

```java
ConnectorId item2d = ConnectorId.createItemId(context.getConnectionId(), "article:someExternalArticle");
exampleItem2 = new ExampleConnectorItem(item2d, context, "External Article", subCategory1);
```

Since we don't want to use the regular type mapping, the _ExampleConnectorItem_ class
has to implement the method _getItemType to return a custom type:

```java
  @Nonnull
  @Override
  public String getItemType() {
    if(id.getExternalId().startsWith("article:")) {
      return "article";
    }
    return ConnectorItem.super.getItemType();
  }
```

We are using the external id of the _ConnectorId_ to identify the type of the item.
If we don't have a custom type, we use the default type mapping by invoking the default implementation.

The preview won't return anything since the default mapping is still active when the preview is generated
and no custom mapping is provided for the item type _article_. We solve this problem
by overriding the _getPreviewHTML_ method:

```java
  @Nullable
  @Override
  public String getPreviewHtml() {
    if(id.getExternalId().startsWith("article:")) {
      return "article";
    }
    return ConnectorItem.super.getPreviewHtml();
  }
```

The preview is rendered with the generated HTML now. Also, the type and icon in the list view has changed.
That is because we set the item type to _article_ which is already mapped in the resource bundles of the Studio Hub plugin.
For more details about the item name and icon mapping, read the chapter "Studio Customization".

### Checkpoint

The custom preview is rendered. The metadata information is hardcoded for every item selection.

![Custom Connector Item with Preview and Metadata](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/item4.png)



## Step 7 - Content Creation

The Studio Hub comes with a default content creator and a default content mapping.
When we setup the connector type definition in the _Connector Types_ settings document, we also linked
the _Content Mapping_ document to it. In there, the connector types are mapping
to the CoreMedia content types available with the Blueprint.
For our picture connector item, we can simply press the "New Content" button of
the toolbar and create a new picture document that contains the image
that is streamed by the _stream_ method of the connector item.


### Checkpoint

The _CMPicture_ document is generated from the connector item "test.png":

![Default Content Creation](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/content1.png)


## Step 8 - Custom Content Creation

In our custom connector item example, we have used the item type _article_
to show how custom type can be specified. When a specific content type should be created you
have to ensure that there is a corresponding mapping for the item type inside the
_Content Mapping_ settings document. Otherwise the default content will be generated,
which is type _CMDownload_.

The creation of new content from our example article will result in the creation of
a new _CMArticle_ document, since there is already an existing entry for it in the _Content Mappings_ settings.

However, when the new article is generated, only the title will be set by the default content creator.
To customize the content creation, we have to implement a separate interceptor that is invoked when
content is created from Studio Hub items.

So, let's create the interceptor class first:

```java
public class ExampleItemWriteInterceptor extends ConnectorItemWriteInterceptor {

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ENTITY)) {
      ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);

      //the intercepts is only applicable when the content was created for an Example Connector entity
      if (entity instanceof ExampleConnectorItem) {
        ExampleConnectorItem exampleItem = (ExampleConnectorItem) entity;
        properties.put("segment", exampleItem.getName().toLowerCase().replaceAll(" ", "-"));
      }
    }
  }
}

```

The interceptor sets the content property _segment_ by formatting the connector item name.
Once the class is declared, we have to add the corresponding Spring configuration inside
the _component-studio-hub-example.xml_ for it:

```xml
  <!-- Content Creation-->
  <bean id="exampleContentItemWriteInterceptor" class="com.coremedia.blueprint.studio.studiohub.ExampleItemWriteInterceptor" parent="connectorItemWriteInterceptor">
    <property name="type" value="CMArticle" />
    <property name="priority" value="0" />
  </bean>
```

The Studio Hub content interceptors are working the same way as existing content interceptors.
That means that maybe other interceptors are executed for the newly created content too.
In that case, the priority attribute allows you to customize the order of execution.

### Checkpoint

You custom content interceptor should have created the URL segment:

![Custom Content Creation](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/content2.png)

