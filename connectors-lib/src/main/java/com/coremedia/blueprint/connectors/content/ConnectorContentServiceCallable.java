package com.coremedia.blueprint.connectors.content;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.rest.cap.content.search.SearchServiceResult;
import com.coremedia.rest.cap.content.search.solr.SolrSearchService;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptor;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.rest.cap.intercept.impl.ContentWriteRequestImpl;
import com.coremedia.rest.validation.Issues;
import com.coremedia.rest.validation.impl.IssuesImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The callable used for asynchronous content creation.
 */
class ConnectorContentCallable implements Callable<Void> {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorContentCallable.class);

  private final ConnectorContext context;
  private final ConnectorEntity entity;
  private List<ContentWriteInterceptor> contentWriteInterceptors;
  private SolrSearchService solrSearchService;
  private final Content content;

  ConnectorContentCallable(ConnectorContext context,
                           Content content,
                           ConnectorEntity entity,
                           List<ContentWriteInterceptor> contentWriteInterceptors,
                           SolrSearchService solrSearchService) {
    this.content = content;
    this.context = context;
    this.entity = entity;
    this.contentWriteInterceptors = contentWriteInterceptors;
    this.solrSearchService = solrSearchService;
  }

  @Override
  public Void call() {
    try {
      Thread.currentThread().setName("Connector Content Service Callable for " + entity);
      if (content.isDocument() && !content.isCheckedOut()) {
        content.checkOut();
      }

      //Write Interceptors may create content again that trigger interceptors again
      //We want to re-use contents if they exists, so they must be searchable.
      waitUntilFeeded(content, 30, 100);

      //create a dummy write request which collects the data of all applicable write interceptors
      Issues issues = new IssuesImpl<>(content, Collections.emptyList());
      Map<String, Object> properties = new HashMap<>();
      properties.put(ConnectorItemWriteInterceptor.CONNECTOR_ENTITY, entity);
      properties.put(ConnectorItemWriteInterceptor.CONTENT_ITEM, content);
      properties.put(ConnectorItemWriteInterceptor.CONNECTOR_CONTEXT, context);
      ContentWriteRequest request = new ContentWriteRequestImpl(content, content.getParent(), content.getName(), content.getType(), properties, issues);

      //trigger interceptor execution
      executeInterceptors(request);

      //write all values that have been collected by the interceptors
      writeInterceptorsProperties(properties);
    } catch (Exception e) {
      LOG.error("Error creating item data for content " + content.getPath() + ": " + e.getMessage(), e);
    } finally {
      if (content.isDocument() && content.isCheckedOut()) {
        content.checkIn();
      }
    }
    return null;
  }

  /**
   * The given properties have been collected by all applicable write interceptors.
   * We simply have to apply them.
   * @param properties the interceptor properties
   */
  private void writeInterceptorsProperties(Map<String, Object> properties) {
    //the try to apply the interceptor data
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value != null) {
        CapPropertyDescriptor descriptor = content.getType().getDescriptor(key);
        if (descriptor != null) {
          if (!content.isCheckedOut()) {
            content.checkOut();
          }

          //do no overwrite existing values
          Object existingValue = content.get(key);
          if (existingValue instanceof String && !StringUtils.isEmpty(String.valueOf(existingValue))) {
            continue;
          }

          content.set(key, value);
        }
      }
    }
    content.getRepository().getConnection().flush();
  }

  /**
   * Triggers all write interceptors that are applicable for the current content.
   * @param request the write request that contains all data
   */
  private void executeInterceptors(ContentWriteRequest request) {
    //let all interceptors run on the newly created item content
    for (ContentWriteInterceptor contentWriteInterceptor : new ArrayList<>(contentWriteInterceptors)) {
      try {
        ContentType contentType = contentWriteInterceptor.getType();
        if (content.getType().isSubtypeOf(contentType)) {
          contentWriteInterceptor.intercept(request);
          content.getRepository().getConnection().flush();
        }
      } catch (Exception e) {
        LOG.error("Failed to execute " + contentWriteInterceptor + ": " + e.getMessage(), e);
      }
    }
  }

  /**
   * Search for a content with the given id with exponential waiting time
   * @param content the content to find
   * @param timeoutSeconds the timeout in seconds
   * @param intervalMillis the intervall that is increased
   */
  private void waitUntilFeeded(Content content, long timeoutSeconds, long intervalMillis) {
    if (timeoutSeconds < 0) {
      return;
    }

    SearchServiceResult result = solrSearchService.search("id:" + IdHelper.parseContentId(content.getId()), -1,
            new ArrayList<>(),
            content.getRepository().getRoot(),
            true,
            Arrays.asList(content.getType()),
            false,
            Collections.emptyList(),
            new ArrayList<>(),
            new ArrayList<>());
    if (!result.getHits().isEmpty()) {
      return;
    }

    try {
      Thread.sleep(intervalMillis);
      long newTimeout = timeoutSeconds * 1000 - intervalMillis;
      long newInterval = intervalMillis * 2;
      waitUntilFeeded(content, newTimeout, newInterval);
    } catch (InterruptedException e) {
      //ignore
    }
  }
}