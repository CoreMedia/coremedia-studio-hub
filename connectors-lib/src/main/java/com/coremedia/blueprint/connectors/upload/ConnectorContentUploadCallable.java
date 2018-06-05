package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.content.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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

  ConnectorContentUploadCallable(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category, @Nonnull List<Content> contents, Boolean defaultAction) {
    this.context = context;
    this.category = category;
    this.contents = contents;
    this.defaultAction = defaultAction;
  }

  @Override
  public Void call() {
    try {
      Thread.currentThread().setName("Connector Content Service Callable for " + category);
      category.uploadContent(context, contents, defaultAction);
    } catch (Exception e) {
      LOG.error("Error creating item data for category " + category + ": " + e.getMessage(), e);
    }
    return null;
  }
}