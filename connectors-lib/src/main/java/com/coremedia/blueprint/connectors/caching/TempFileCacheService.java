package com.coremedia.blueprint.connectors.caching;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.cap.common.TempFileService;
import com.coremedia.cap.content.ContentRepository;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This ensures that remote folders or file information are not read multiple times.
 */
public class TempFileCacheService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TempFileCacheService.class);

  private ConcurrentLinkedQueue<TempFile> cache = new ConcurrentLinkedQueue<>();
  private int cacheSize = 100;

  public TempFile createTempFile(ContentRepository contentRepository, ConnectorItem item) throws IOException {
    try {
      TempFile tempFile = findTempFile(item);
      if(tempFile != null) {
        return tempFile;
      }

      InputStream in = item.stream();
      if (in == null) {
        return null;
      }

      String prefix = createFileId(item);
      TempFileService tempFileService = contentRepository.getConnection().getTempFileService();
      File assetTempFile = tempFileService.createTempFileFor(prefix, "item");

      FileUtils.copyToFile(in, assetTempFile);
      in.close();

      TempFile entry = new TempFile(prefix, assetTempFile);
      cache.add(entry);

      while (cache.size() > cacheSize) {
        TempFile poll = cache.poll();
        poll.delete();
      }

      return entry;
    } catch (IOException e) {
      LOGGER.error("Failed to create temp file for " + item.getConnectorId().toString() + ":" + e.getMessage(), e);
      throw e;
    }
  }

  public TempFile findTempFile(ConnectorItem item) {
    String id = createFileId(item);

    if (cache.contains(new TempFile(id, null))) {
      for (TempFile tempFile : cache) {
        if(tempFile.getId().equals(id)) {
          if (tempFile.getFile().exists()) {
            return tempFile;
          }
          else {
            cache.remove(tempFile);
          }
        }
      }
    }
    return null;
  }

  public void clear(ConnectorContext context) {
    for (TempFile cacheEntry : new ArrayList<>(cache)) {
      if (cacheEntry.getFile().exists()) {
        String connectionId = cacheEntry.getId().split("-")[0];
        if(connectionId.equals(context.getConnectionId())) {
          boolean deleted = cacheEntry.delete();
          if(deleted) {
            cache.remove(cacheEntry);
          }
          else {
            LOGGER.warn("Failed to delete connector preview temp file " + cacheEntry.getFile().getAbsolutePath());
          }
        }
      }
    }
  }

  private String createFileId(ConnectorItem item) {
    ConnectorId id = item.getConnectorId();
    return id.getConnectionId() + "-" + id.getExternalId();
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }
}
