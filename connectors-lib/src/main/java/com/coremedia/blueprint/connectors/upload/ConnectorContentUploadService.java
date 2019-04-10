package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 */
public class ConnectorContentUploadService {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorContentUploadService.class);

  private ExecutorService service = Executors.newCachedThreadPool();

  private Connectors connectors;
  private List<ConnectorContentUploadInterceptor> connectorContentUploadInterceptors;
  private ConnectorImageTransformationService transformationService;

  ConnectorContentUploadService(@NonNull Connectors connectors,
                                @NonNull ConnectorImageTransformationService transformationService,
                                @Nullable List<ConnectorContentUploadInterceptor> connectorContentUploadInterceptors) {
    this.connectors = connectors;
    this.connectorContentUploadInterceptors = connectorContentUploadInterceptors;
    this.transformationService = transformationService;
  }

  public void upload(@NonNull ConnectorContext context,
                     @NonNull ConnectorCategory category,
                     @NonNull List<Content> contents,
                     @NonNull List<String> userSelectedPropertyNames,
                     Boolean defaultAction) {
    new Thread(() -> {
      Thread.currentThread().setName("Connector Upload Service Thread for " + category.getConnectorId());
      try {
        List<ConnectorContentUploadInterceptor> interceptors = new ArrayList<>();

        if (connectorContentUploadInterceptors != null && !connectorContentUploadInterceptors.isEmpty()) {
          for (ConnectorContentUploadInterceptor interceptor : connectorContentUploadInterceptors) {
            if (interceptor.getConnectionId() == null || interceptor.getConnectionId().equals(context.getConnectionId())) {
              interceptors.add(interceptor);
            }
          }
          //TODO not sure if the direction is right here
          Collections.sort(interceptors, Comparator.comparingInt(ConnectorContentUploadInterceptor::priority));
        }

        ConnectorContentUploadCallable callable = new ConnectorContentUploadCallable(context, category, contents, userSelectedPropertyNames, interceptors, transformationService, defaultAction);
        Future<Void> submit = service.submit(callable);
        submit.get();
        category.refresh(context);
        connectors.notifyCategoryChange(context, category);
      } catch (Exception e) {
        LOG.error("Error submitting connector content callable for " + category + ": " + e.getMessage(), e);
      }
    }).start();

  }
}
