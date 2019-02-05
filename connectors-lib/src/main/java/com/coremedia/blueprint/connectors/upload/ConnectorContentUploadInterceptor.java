package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

/**
 * Interceptor interface used for custom uploads.
 */
public interface ConnectorContentUploadInterceptor {

  /**
   * Returns the connection id this interceptor is applicable for
   */
  @NonNull
  String getConnectionId();

  /**
   * The priority of this interceptor. The lower the priority, the higher it is.
   */
  int priority();

  /**
   * Intercepts the default upload of the given content.
   * This contains the actual implementation of the upload.
   * @param context the context of the connection
   * @param category the category the content should be uploaded to
   * @param contents the contents that should be uploaded
   * @param propertyNames the user selected property names
   */
  void intercept(@NonNull ConnectorContext context,
                 @NonNull ConnectorCategory category,
                 @NonNull List<Content> contents,
                 @NonNull List<String> propertyNames);
}
