# Content Push

The Content Hub not only allows to create content from external systems data, but also to push content to them.
This section describes which implementation and configuration issues have to be taken into account to support a content push.

To enable the content push for your connector, make sure that you have a settings document _contentUploadTypes_ linked
in your _Connector Types_ settings. The default document linked in the Content Hub demo content is called _Content Upload Types_. 

The _Content Upload Types_ document describes which CoreMedia document types are allowed for uploading
and what are the property names to extract for pushing. By default, the content upload is enabled when a _connector category_ is writeable
or the _isContentUploadEnabled()_ method of the _connector category_ returns true.

If all of these conditions are met, the top level categories for every content hub connection are shown in the content
push menu that is available in the repository/search toolbar of the Studio library and the premular toolbar of a content tab. 
 
By default, the _upload_ method of the _connector category_ is used to upload content blobs to the specific target system.

![Content Push Menu](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/push_dropdown.png)

## Push Dialog

Once a connection is selected content should be pushed to, the push dialog opens.

![Content Push Dialog](https://github.com/CoreMedia/coremedia-studio-hub/blob/master/documentation/images/push_dialog.png)

The dialog allows to select a target folder/category the properties should be pushed too.
Additional, the user may select which content properties to push. The list of properties shown is matches the one 
configured in the _Content Upload Types_ setting.
  

## Default Push Behaviour

The Content Hub comes with a default behaviour that is executed if not _ConnectorContentUploadInterceptor_ is configured
for the current push. The following table shows the behaviour for each or the different property types_


| Type | Supported | Behaviour |  
| ---- |: --------- :| --------- |
| INTEGER   | - |  |  
| DATE      | - |  |
| STRING    | x | The value of the property is written as a text file to the target. |
| BLOB      | x | The blob is converted to a file, depending on the mime-type and written to the target. |
| LINK      | x | Links are followed up until the depth of 1. When the linked content type is matching an existing upload mapping, the matching properties are uploaded. |
| MARKUP    | x | The value of the property converted to text and written to the target. | 
| BOOLEAN   | - |  |
| INT       | - |  |
| STRUCT    | - |  |


## Pushing Image (Variants)

When a blob of a content has an image mime type, the Content Hub will try to push the configured image variants.
The image variants that should be pushed can be configured in the _Connections_ settings struct of the target, e.g.

```xml
<StringProperty Name="imageVariants">portrait_ratio1x1,landscape_ratio16x9,landscape_ratio4x3</StringProperty>
```

The variants are stored as comma separated value. If the picture content does not provide the given variant, the upload 
is skipped. Otherwise, the target possible variant is uploaded to the target system.

If the image is configured for using the original image, the variant upload is skipped and the original image is uploaded instead.

## Intercepting Content Push

The Content Hub allows to intercept the default content push by implementing _ConnectorContentUploadInterceptor_ classes.
For every content push, the Content Hub will check which interceptors are available and applicable for the given content to push.
If there is at least one applicable instance, the default upload won't be executed anymore and the _intercept_ method
of the interceptor has to take care of the complete push itself.

## Push Results

//TODO this will come with the new jobs API 1904

