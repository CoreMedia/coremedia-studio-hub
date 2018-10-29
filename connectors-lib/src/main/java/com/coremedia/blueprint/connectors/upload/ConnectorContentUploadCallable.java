package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
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

  private final ConnectorContext context;
  private final ConnectorCategory category;
  private final List<Content> contents;
  private final Boolean defaultAction;

  ConnectorContentUploadCallable(@NonNull ConnectorContext context, @NonNull ConnectorCategory category, @NonNull List<Content> contents, Boolean defaultAction) {
    this.context = context;
    this.category = category;
    this.contents = contents;
    this.defaultAction = defaultAction;
  }

  @Override
  public Void call() {
    try {
      Thread.currentThread().setName("Connector Content Service Callable for " + category);
      uploadContent(category, context, contents, defaultAction);
    } catch (Exception e) {
      LOG.error("Error creating item data for category " + category + ": " + e.getMessage(), e);
    }
    return null;
  }


  /**
   * Default interface for content drop.
   * The methods has to be implemented for categories that allow content drops
   *
   * @param category
   * @param contents the contents that have been dropped on the category.
   * @param defaultAction true, if the default action was used (no CTRL was pressed)
   * @return true if the drop was successful
   */
  private boolean uploadContent(ConnectorCategory category, @NonNull ConnectorContext context, @NonNull List<Content> contents, Boolean defaultAction) {
    for (Content content : contents) {
      List<CapPropertyDescriptor> descriptors = content.getType().getDescriptors();
      for (CapPropertyDescriptor descriptor : descriptors) {
        if(descriptor.getType().equals(CapPropertyDescriptorType.BLOB)) {
          Blob blob = content.getBlob(descriptor.getName());
          if(blob != null && blob.getContentType() != null) {
            MimeType contentType = blob.getContentType();
            String name = content.getName() + "." + contentType.getSubType();
            return category.upload(context, name, blob.getInputStream()) != null;
          }
        }
      }
    }

    return false;
  }

}