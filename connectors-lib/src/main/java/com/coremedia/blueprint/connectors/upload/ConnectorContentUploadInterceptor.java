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
   * Returns the connection id this interceptor is applicable for.
   * If null is returned, the interceptor will be used for all uploads.
   */
  @NonNull
  String getConnectionId();

  /**
   * The priority of this interceptor. The lower the priority, the higher it is.
   */
  int priority();

  boolean isApplicable(@NonNull ConnectorContext context,
                       @NonNull Content content);

  /**
   * Intercepts the default upload of the given content.
   * This contains the actual implementation of the upload.
   *
   * @param context       the context of the connection
   * @param category      the category the content should be uploaded to
   * @param content       the content to use for uploading properties
   * @param propertyNames the user property names to upload
   */
  void intercept(@NonNull ConnectorContext context,
                 @NonNull ConnectorCategory category,
                 @NonNull Content content,
                 @NonNull List<String> propertyNames);
}
