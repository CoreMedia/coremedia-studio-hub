# Content Creation

This section describes how content is created by the Content Hub and how to customize it.

### Asynchronous Content Creation

The content that is created for a selection of a connector item depends on the type that is configured
in the _Content Mapping_ document of the corresponding connector type configuration. While the content for an item
is created and opened immediately (if created via dialog), the postprocessing of the content is executed by
the Java backend of the Content Hub. For the execution time of the postprocessing, the content is locked
by the server. This ensures that the document of the external system can downloaded, converted and put into a content
property without concurrent access of a Studio user. Once the postprocessing is finished, the access to the newly 
created content is released. Note that every content generated from the Content Hub contains the 'ConnectorId'
to identify the connector and item it has been generated from.

### Bulk Content Creation

The connector framework's Studio part allows to drop a bulk list of items from the list view
into the content repository tree. All dropped items will be converted into CoreMedia content.
Unlike a single content creation via 'QuickCreate' dialog, the newly generated content
of bulk operations won't be opened in a new Studio tab.

### Content Scopes

A duplicate content creation can be avoided by setting the string property 'contentScope' inside a
connection configuration to one of the following values:

| Value | Description |
| ----- | ----------- |
| site | the content for a connector item is only created once per site |
| domain | the content for a connector item is only created once per domain |
| global | the content for a connector item is only created once |
| -EMPTY- | no check is performed and duplicate content creation is allowed |

For bulk content creation, the items for which a content exists will be skipped.
For the content creation dialog, a warning will pop-up, that an existing content already exists.
The user may continue anyway or show the item in the library.

### Content Write Interceptors

The postprocessing of newly created content is implemented using the existing content write interceptor framework.
The Content Hub searches for all matching write interceptors and executes a _ContentWriteRequest_ for them.
Therefore all existing _ContentWriteInterceptor_ will be applied for these contents too.

The default implementation for filling additional content properties for a connector item 
is implemented in the class _ContentItemWriteInterceptor_. The class used the attributes of a connector item to fill
the corresponding properties of a _CMTeasable_ document.

If additional content should be created out of a connector item, e.g. pictures should be extracted from a RSS feed entry, 
additional write interceptors can be implemented that extract the connector item from the _ContentWriteRequest_ and process
the data individually. A good example for this can be looked up in the class _RssContentItemWriteInterceptor.java_.

### Recursive Content Write Interceptors Calls

If an external system provides complex data structures with additional dependencies to other items, it may be necessary
to create CoreMedia content for these dependencies first.
This can be achieved by injecting the _ConnectorContentCreationService_ to the _ContentWriteInterceptor_ of the
specific connector.

For example, there is an article in an external system we want to create a CoreMedia article from.
If this article references a picture, we want to create a CoreMedia picture first.
So we would first lookup if the picture already exists, then create the _CMPicture_ document otherwise
and finally and link it to the newly created article.

The following code inside the corresponding _ContentWriteInterceptor_ would look like this:

```java
//given: we have generated the connector id of the picture, so we look up existing item and content first
MyConnectorItem externalPictureItem = (MyConnectorItem) myConnectorService.getItem(context, pictureConnectorId);

Content pictureContent = connectorContentService.findContent(pictureConnectorId, site);
if (pictureContent != null) {
  //yes, the picture exists, so link it to the owner/article
  linkContent(owner, pictureContent);
  return;
}

//we have to create the picture first and put it into the same folder like the article
pictureContent = connectorContentService.createContent(pictureConnectorId, owner.getParent().getPath(), site);

//we have to trigger the post processing to initialize the newly created image
connectorContentService.processContent(pictureContent, externalPictureItem, true);

//finally link the new picuture
linkContent(owner, pictureContent);
```
