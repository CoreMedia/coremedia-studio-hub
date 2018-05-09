package com.coremedia.blueprint.connectors.filesystems;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import net.sf.ehcache.CacheManager;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * This ensures that remote folders or file information are not read multiple times.
 */
public class FileSystemService {

  @Cacheable(value = "connectorItems", key = "#context.connectionId + '_' + #categoryId.externalId")
  public FileSystemItem listItems(FileBasedConnectorService service, ConnectorContext context, ConnectorId categoryId) {
    List<Object> items = service.list(categoryId);
    Object folderData = service.getFile(categoryId);
    return new FileSystemItem<>(folderData, items);
  }

  public void invalidate() {
    CacheManager cacheManager = CacheManager.getInstance();
    cacheManager.getCache("connectorItems").removeAll();
  }
}
