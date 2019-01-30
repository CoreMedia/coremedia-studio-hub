package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The callable used for asynchronous content upload.
 */
class ConnectorContentUploadCallable implements Callable<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorContentUploadCallable.class);
  private static final String MIME_TYPE_IMAGE = "image";

  private final ConnectorContext context;
  private final ConnectorCategory category;
  private final List<Content> contents;
  private ConnectorImageTransformationService transformationService;
  private final Boolean defaultAction;

  /**
   * Callable for content drop.
   *
   * @param context               context of the connection
   * @param category              the category to create the new item in
   * @param contents              the contents that have been dropped on the category.
   * @param transformationService service to transform images before uploading
   * @param defaultAction         true, if the default action was used (no CTRL was pressed)
   */
  ConnectorContentUploadCallable(@NonNull ConnectorContext context,
                                 @NonNull ConnectorCategory category,
                                 @NonNull List<Content> contents,
                                 @NonNull ConnectorImageTransformationService transformationService,
                                 Boolean defaultAction) {
    this.context = context;
    this.category = category;
    this.contents = contents;
    this.transformationService = transformationService;
    this.defaultAction = defaultAction;
  }

  @Override
  public Void call() {
    try {
      Thread.currentThread().setName("Connector Content Service Callable for " + category);
      uploadContent(context, category, contents, defaultAction);
    } catch (Exception e) {
      LOG.error("Error creating item data for category " + category + ": " + e.getMessage(), e);
    }
    return null;
  }


  /**
   * Default interface for content drop.
   * The methods has to be implemented for categories that allow content drops
   *
   * @param context       the context of the connection.
   * @param category      the category to create the new item in
   * @param contents      the contents that have been dropped on the category.
   * @param defaultAction true, if the default action was used (no CTRL was pressed)
   */
  private void uploadContent(@NonNull ConnectorContext context, @NonNull ConnectorCategory category, @NonNull List<Content> contents, Boolean defaultAction) {
    for (Content content : contents) {
      List<String> blobPropertyNames = context.getContentUploadTypes().getBlobPropertyNames(content.getType());
      if(blobPropertyNames.isEmpty()) {
        LOG.warn("No blob property mapping found for content type '" + content.getType().getName() + "'");
      }

      for (String blobPropertyName : blobPropertyNames) {
        try {
          Blob blob = content.getBlob(blobPropertyName);
          if (blob == null || blob.getContentType() == null) {
            continue;
          }

          MimeType contentType = blob.getContentType();
          String name = content.getName() + "." + contentType.getSubType();

          if (blob.getContentType().getPrimaryType().equalsIgnoreCase(MIME_TYPE_IMAGE) && !context.getImageVariants().isEmpty()) {
            List<String> imageVariants = context.getImageVariants();
            List<TransformedBlob> transformedBlobs = this.transformationService.transform(context, content, blobPropertyName, blob, imageVariants);
            for (TransformedBlob transformedBlob : transformedBlobs) {
              name = content.getName() + "[" + transformedBlob.getVariant() + "]." + contentType.getSubType();
              category.upload(context, name, contentType, transformedBlob.getBlob().getInputStream());
            }
          }
          else {
            category.upload(context, name, contentType, blob.getInputStream());
          }
        } catch (Exception e) {
          LOG.error("Failed to upload blob data of " + content.getPath() + ": " + e.getMessage(), e);
        }
      }
    }
  }

}
