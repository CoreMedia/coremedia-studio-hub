package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The callable used for asynchronous content upload.
 */
class ConnectorContentUploadCallable implements Callable<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorContentUploadCallable.class);
  private static final String MIME_TYPE_IMAGE = "image";
  private static final int MAX_EXPORT_DEPTH = 1;

  private final ConnectorContext context;
  private final ConnectorCategory category;
  private final List<Content> contents;
  private List<String> propertyNames;
  private final List<ConnectorContentUploadInterceptor> uploadInterceptors;
  private ConnectorImageTransformationService transformationService;
  private final Boolean defaultAction;
  private int exportDepth = 0;

  /**
   * Callable for content drop.
   *
   * @param context               context of the connection
   * @param category              the category to create the new item in
   * @param contents              the contents that have been dropped on the category.
   * @param propertyNames         the user selection of property names
   * @param transformationService service to transform images before uploading
   * @param defaultAction         true, if the default action was used (no CTRL was pressed)
   */
  ConnectorContentUploadCallable(@NonNull ConnectorContext context,
                                 @NonNull ConnectorCategory category,
                                 @NonNull List<Content> contents,
                                 @NonNull List<String> propertyNames,
                                 @NonNull List<ConnectorContentUploadInterceptor> uploadInterceptors,
                                 @NonNull ConnectorImageTransformationService transformationService,
                                 Boolean defaultAction) {
    this.context = context;
    this.category = category;
    this.contents = contents;
    this.propertyNames = propertyNames;
    this.uploadInterceptors = uploadInterceptors;
    this.transformationService = transformationService;
    this.defaultAction = defaultAction;
  }

  @Override
  public Void call() {
    try {
      Thread.currentThread().setName("Connector Content Service Callable for " + category);

      if (!uploadInterceptors.isEmpty()) {
        for (ConnectorContentUploadInterceptor uploadInterceptor : uploadInterceptors) {
          uploadInterceptor.intercept(context, category, contents, propertyNames);
        }
      }
      else {
        //should never happen, catched by the UI
        if (context.getContentUploadTypes() == null) {
          return null;
        }

        //well, all types are the same from UI point of view, but it doesn't hurt to handle different types here
        for (Content content : contents) {
          List<String> defaultPropertyNames = context.getContentUploadTypes().getPropertyNames(content.getType());
          if (defaultPropertyNames.isEmpty()) {
            LOG.info("No connector upload property mapping found for content type '" + content.getType().getName() + "'");
          }

          if(!propertyNames.isEmpty()) {
            defaultPropertyNames = propertyNames;
          }

          executeDefaultUpload(content, defaultPropertyNames);
        }
      }
    } catch (Exception e) {
      LOG.error("Error creating item data for category " + category + ": " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * The default upload implementation for connectors.
   * If no ConnectorContentUploadInterceptor is configured for the given connection
   * this upload strategy will be executed.
   *
   * @param content the content to upload.
   */
  private void executeDefaultUpload(Content content, List<String> propertyNames) {
    exportDepth++;
    for (String propertyName : propertyNames) {
      try {
        CapPropertyDescriptor descriptor = content.getType().getDescriptor(propertyName);
        CapPropertyDescriptorType propertyType = descriptor.getType();
        if (propertyType.equals(CapPropertyDescriptorType.BLOB)) {
          uploadBlobContent(content, propertyName);
        }
        else if (propertyType.equals(CapPropertyDescriptorType.STRING)) {
          String name = content.getName() + "_[" + propertyName + "].txt";
          String value = content.getString(propertyName);
          if (!StringUtils.isEmpty(value)) {
            InputStream in = new ByteArrayInputStream(value.getBytes(Charset.defaultCharset()));
            category.upload(context, name, new MimeType("plain", "text"), in);
          }
        }
        else if (propertyType.equals(CapPropertyDescriptorType.MARKUP)) {
          String name = content.getName() + "_[" + propertyName + "].txt";
          Markup markup = content.getMarkup(propertyName);
          String plainText = MarkupUtil.asPlainText(markup);
          if (!StringUtils.isEmpty(plainText)) {
            InputStream in = new ByteArrayInputStream(plainText.getBytes(Charset.defaultCharset()));
            category.upload(context, name, new MimeType("plain", "text"), in);
          }
        }
        else if(propertyType.equals((CapPropertyDescriptorType.LINK)) && exportDepth <= MAX_EXPORT_DEPTH) {
          List<Content> links = content.getLinks(propertyName);
          for (Content link : links) {
            List<String> linkedPropertyNames = context.getContentUploadTypes().getPropertyNames(link.getType());
            executeDefaultUpload(link, linkedPropertyNames);
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to upload blob data of " + content.getPath() + ": " + e.getMessage(), e);
      }
    }
  }

  private void uploadBlobContent(Content content, String propertyName) throws Exception {
    Blob blob = content.getBlob(propertyName);
    if (blob == null || blob.getContentType() == null) {
      return;
    }

    MimeType mimeType = blob.getContentType();
    String name = content.getName() + "." + mimeType.getSubType();

    if (blob.getContentType().getPrimaryType().equalsIgnoreCase(MIME_TYPE_IMAGE) && !context.getImageVariants().isEmpty()) {
      List<String> imageVariants = context.getImageVariants();
      List<TransformedBlob> transformedBlobs = this.transformationService.transform(context, content, propertyName, blob, imageVariants);
      for (TransformedBlob transformedBlob : transformedBlobs) {
        name = content.getName() + "_[" + transformedBlob.getVariant() + "]." + mimeType.getSubType();
        category.upload(context, name, mimeType, transformedBlob.getBlob().getInputStream());
      }
    }
    else {
      category.upload(context, name, mimeType, blob.getInputStream());
    }
  }
}
