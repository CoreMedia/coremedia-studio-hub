package com.coremedia.blueprint.connectors.dropbox;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.filesystems.FileBasedConnectorService;
import com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.RateLimitException;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.ACCESS_TOKEN;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.APP_NAME;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.DISPLAY_NAME;

public class DropboxConnectorServiceImpl extends FileBasedConnectorService<Metadata> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DropboxConnectorServiceImpl.class);

  private DropboxConnectorCategory rootCategory;
  private DbxClientV2 client;

  private List<String> latestEntries = new ArrayList<>();

  @Override
  public boolean init(@NonNull ConnectorContext context) {
    super.init(context);

    String accessToken = context.getProperty(ACCESS_TOKEN);
    String displayName = context.getProperty(DISPLAY_NAME);

    if (accessToken == null || accessToken.trim().length() == 0) {
      throw new ConnectorException("No accessToken configured for Dropbox connection " + context.getConnectionId());
    }

    try {
      DbxRequestConfig config = new DbxRequestConfig(displayName);

      String proxyHost = context.getProperty(ConnectorPropertyNames.PROXY_HOST);
      String proxyPort = context.getProperty(ConnectorPropertyNames.PROXY_PORT);
      String proxyType = context.getProperty(ConnectorPropertyNames.PROXY_TYPE);

      if(proxyType != null && proxyHost != null && proxyPort != null) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
        Proxy proxy = new Proxy(Proxy.Type.valueOf(proxyType.toUpperCase()), inetSocketAddress);
        StandardHttpRequestor.Config requestorConfig = StandardHttpRequestor.Config.builder().withProxy(proxy).build();
        StandardHttpRequestor requestor = new StandardHttpRequestor(requestorConfig);

        config = DbxRequestConfig.newBuilder(displayName)
                .withHttpRequestor(requestor)
                .build();
      }

      client = new DbxClientV2(config, accessToken);
      return true;
    } catch (Exception e) {
      throw new ConnectorException("Failed to create Dropbox client: " + e.getMessage(), e);
    }
  }

  @Override
  public InvalidationResult invalidate(@NonNull ConnectorContext context) {
    InvalidationResult invalidationResult = new InvalidationResult(context);

    List<String> refreshedEntries = getAllEntries();
    if(latestEntries == null || latestEntries.isEmpty()) {
      latestEntries = refreshedEntries;
      return null;
    }


    int added = 0;
    int deleted = 0;

    //find new entries
    List<String> dirtyItems = new ArrayList<>();
    for (String entry : refreshedEntries) {
      if(!latestEntries.contains(entry)) {
        dirtyItems.add(entry);
        added++;
      }
    }

    //find deleted entries
    for (String latestEntry : latestEntries) {
      if(!refreshedEntries.contains(latestEntry)) {
        dirtyItems.add(latestEntry);
        deleted++;
      }
    }

    //prepare invalidation result
    if(deleted > 0 || added > 0) {
      for (String dirtyItem : dirtyItems) {
        ConnectorId id = ConnectorId.createItemId(context.getConnectionId(), dirtyItem);
        ConnectorId folderId = getFolderId(id);
        ConnectorCategory category = getCategory(context, folderId);
        invalidationResult.addEntity(category);
      }

      refresh(context, getRootCategory(context));
      invalidationResult.addMessage("dropbox", rootCategory, Arrays.asList(rootCategory.getName(), added, deleted));
      invalidationResult.addEntity(rootCategory);
    }

    latestEntries = refreshedEntries;
    return invalidationResult;
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      String displayName = context.getProperty(DISPLAY_NAME);

      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new DropboxConnectorCategory(this, null, context, null, id);
      rootCategory.setName(displayName);

      List<ConnectorCategory> subCategories = getSubCategories(rootCategory);
      rootCategory.setSubCategories(subCategories);

      List<ConnectorItem> items = getItems(rootCategory);
      rootCategory.setItems(items);
    }
    return rootCategory;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId itemId) throws ConnectorException {
    ConnectorId parentFolderId = getFolderId(itemId);
    Metadata file = getCachedFileOrFolderEntity(context, itemId);
    if(file == null) {
      LOGGER.warn("Dropbox item not found for connector id " + itemId);
      return null;
    }
    return new DropboxConnectorItem(this, getCategory(context, parentFolderId), context, file, itemId);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId categoryId) throws ConnectorException {
    ConnectorCategory parentCategory = getParentCategory(context, categoryId);
    if (parentCategory == null) {
      return getRootCategory(context);
    }

    Metadata metadata = getCachedFileOrFolderEntity(context, categoryId);
    DropboxConnectorCategory subCategory = new DropboxConnectorCategory(this, parentCategory, context, metadata, categoryId);
    subCategory.setItems(getItems(subCategory));
    subCategory.setSubCategories(getSubCategories(subCategory));
    return subCategory;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();
    String path = category.getConnectorId().getExternalId();
    if (category.getConnectorId().isRootId()) {
      path = "";
    }

    if (searchType == null && query.equals("*")) {
      results.addAll(getSubCategories(category));
      results.addAll(getItems(category));
    }
    else if (searchType != null && query.equals("*")) {
      List<ConnectorItem> items = getItems(category);
      for (ConnectorItem item : items) {
        if (item.isMatchingWithItemType(searchType) && item.getParent().getConnectorId().equals(category.getConnectorId())) {
          results.add(item);
        }
      }
    }
    else {
      try {
        if (query == null || query.equals("*")) {
          query = "";
        }
        SearchResult search = client.files().search(path, query);
        List<SearchMatch> matches = search.getMatches();
        for (SearchMatch match : matches) {
          Metadata metadata = match.getMetadata();

          if (metadata instanceof FolderMetadata) {
            ConnectorId id = ConnectorId.createCategoryId(context.getConnectionId(), getPath(metadata));
            ConnectorCategory cat = getCategory(context, id);
            if (searchType == null || searchType.equals(ConnectorCategory.DEFAULT_TYPE)) {
              results.add(cat);
            }
          }
          else {
            ConnectorId id = ConnectorId.createItemId(context.getConnectionId(), getPath(metadata));
            ConnectorItem item = getItem(context, id);
            if (item.isMatchingWithItemType(searchType) && item.getParent().getConnectorId().equals(category.getConnectorId())) {
              results.add(item);
            }
          }
        }
      } catch (DbxException e) {
        throw new ConnectorException(e);
      }
    }

    return new ConnectorSearchResult<>(results);
  }

  public boolean refresh(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    if (category.getConnectorId().isRootId()) {
      rootCategory = null;
      rootCategory = (DropboxConnectorCategory) getRootCategory(context);
    }
    return super.refresh(context, category);
  }

  public ConnectorItem upload(@NonNull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    try {
      String uniqueObjectName = createUniqueFilename(context, category.getConnectorId(), itemName);
      ConnectorId newItemId = ConnectorId.createItemId(context.getConnectionId(), uniqueObjectName);
      client.files().uploadBuilder(newItemId.getExternalId()).uploadAndFinish(inputStream);
      inputStream.close();
      this.refresh(context, category);
      return getItem(context, newItemId);
    } catch (Exception e) {
      LOGGER.error("Failed to upload " + itemName + ": " + e.getMessage(), e);
      throw new ConnectorException(e);
    }
  }

  boolean delete(DropboxConnectorItem item) throws ConnectorException {
    try {
      String path = item.getConnectorId().getExternalId();
      client.files().deleteV2(path);
      refresh(context, item.getParent());
      return true;
    } catch (DbxException e) {
      throw new ConnectorException(e);
    }
  }

  InputStream stream(DropboxConnectorItem item) throws ConnectorException {
    try {
      String path = item.getConnectorId().getExternalId();
      return client.files().download(path).getInputStream();
    } catch (DbxException e) {
      throw new ConnectorException(e);
    }
  }

  String getAppName() {
    return context.getProperty(APP_NAME);
  }

  //---------------------------File System Connector -------------------------------------------------------------------

  public List<Metadata> list(ConnectorId categoryId) {
    String path = categoryId.getExternalId();
    if (categoryId.isRootId()) {
      path = "";
    }
    try {
      ListFolderResult listFolderResult = client.files().listFolder(path);
      return listFolderResult.getEntries();
    } catch (RateLimitException rle) {
      LOGGER.error("Failed to list dropbox file list for path '" + path + "': rate limit exceeded.");
      throw new ConnectorException(rle);
    } catch (DbxException e) {
      LOGGER.error("Failed to list dropbox file list for path '" + path + "': " + e.getMessage());
      throw new ConnectorException(e);
    }
  }

  public Metadata getFile(ConnectorId id) {
    String path = id.getExternalId();
    try {
      if (id.isRootId()) {
        return null;
      }
      return client.files().getMetadata(path);
    } catch (DbxException e) {
      throw new ConnectorException("Failed to retrieve dropbox item using path '" + path + ": " + e.getMessage(), e);
    }
  }

  public boolean isFile(Metadata metadata) {
    return metadata instanceof FileMetadata;
  }

  public String getName(Metadata metadata) {
    return metadata.getName();
  }

  public String getPath(Metadata metadata) {
    return metadata.getPathDisplay();
  }

  //----------------------------- Helper -------------------------------------------------------------------------------

  private List<String> getAllEntries() {
    List<String> allEntries = new ArrayList<>();
    try {
      ListFolderBuilder listFolderBuilder = client.files().listFolderBuilder("");
      ListFolderResult result = listFolderBuilder.withRecursive(true).start();

      for (Metadata entry : result.getEntries()) {
        if (entry instanceof FileMetadata) {
          allEntries.add(entry.getPathDisplay());
        }
      }
    } catch (DbxException e) {
      LOGGER.error("Failed to recursively read all dropbox entries: " + e.getMessage(), e);
    }
    return allEntries;
  }

  private List<ConnectorCategory> getSubCategories(@NonNull ConnectorCategory category) throws ConnectorException {
    List<ConnectorCategory> subCategories = new ArrayList<>();

    List<Metadata> subfolders = getSubfolderEntities(context, category.getConnectorId());
    for (Metadata entry : subfolders) {
      ConnectorId connectorId = ConnectorId.createCategoryId(context.getConnectionId(), getPath(entry));
      DropboxConnectorCategory subCategory = new DropboxConnectorCategory(this, category, context, entry, connectorId);
      subCategory.setItems(getItems(subCategory));
      subCategories.add(subCategory);
    }

    return subCategories;
  }

  private List<ConnectorItem> getItems(@NonNull ConnectorCategory category) throws ConnectorException {
    List<ConnectorItem> items = new ArrayList<>();

    List<Metadata> fileEntities = getFileEntities(context, category.getConnectorId());
    for (Metadata entry : fileEntities) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), getPath(entry));
      DropboxConnectorItem item = new DropboxConnectorItem(this, category, context, entry, itemId);
      items.add(item);
    }

    return items;
  }
}
