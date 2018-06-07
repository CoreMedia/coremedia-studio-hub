# CoreMedia Studio Hub

## Abstract

The CoreMedia Studio Hub allows to integrate various external
and asset management systems into the Studio library and to preview items of these systems.
It allows you to integrate just about any external system or platform into your CoreMedia system.
The Studio Hub is implemented as a Blueprint extension.


## Terms and Description


| Term | Description |
| ---- | ----------- |
| connector | A specific implementation for this extension, e.g. the YouTube or Dropbox connector. |
| (connector) item | A leaf node of an external system representation inside the of the Studio library tree, implemented trough the interface 'ConnectorItem'. |
| (connector) category | A node, folder or collection of other categories and items inside the Studio library tree, implemented trough the interface 'ConnectorCategory'. |
| (connector) service | The service implementation of the connector that implements the retrieval of categories and items. |

## Versioning

The release versions of the Studio Hub is matching the version of CoreMedia workspace releases.
E.g. when the CoreMedia workspace has the version __1804.1__, the matching Studio Hub release will have
version __1804.1-X__ where 'X' indicates the release version of the Studio Hub.

## Features

 * Up- and downloading of items
 * Open items in new browser tabs
 * Open items in the corresponding management system
 * Preview of items (including optional conversions before previewing)
 * Metadata preview of items
 * Creation of CoreMedia content from selected items
 * Bulk content creation of CoreMedia content via drag and drop into the repository
 * Drag'n'drop support to content link-lists
 * Drag'n'drop for content to connector categories
 * Support of content write interceptors (Word to article generation)
 * Automatic invalidation after server side changes
 * Change notifications
 * Custom columns


## Supported Systems

The given tables shows the list of existing Studio Hub connectors and their feature set.


| Feature              | File System | S3  | Dropbox |  RSS | YouTube | CoreMedia | Canto Cumulus | Cloudinary | Navigation |
| -------------------- |:-----------:|:--- |:-------:|:----:|:-------:|:---------:|:-------------:|:----------:|:----------:|
| Content Creation     |     x       |  x  |    x    |  x   |    x    |     x     |        x      |     x      |     -      |
| Preview Support      |     x       |  x  |    x    |  x   |    x    |     x     |        x      |     x      |     x      |
| Metadata Extraction  |     x       |  x  |    x    |  x   |    x    |     x     |        x      |     x      |     x      |
| File Upload          |     x       |  x  |    x    |  -   |    -    |     -     |        x      |     x      |     -      |
| File Download        |     x       |  x  |    x    |  -   |    -    |     x     |        x      |     x      |     -      |
| Invalidation Check   |     -       |  -  |    x    |  x   |    -    |     -     |        -      |     -      |     -      |
| Notifications        |     -       |  -  |    x    |  x   |    -    |     -     |        -      |     -      |     -      |
| Native Drop          |     x       |  x  |    x    |  -   |    -    |     x     |        x      |     x      |     -      |
| Content Drop         |     x       |  x  |    x    |  -   |    -    |     -     |        x      |     x      |     x      |

