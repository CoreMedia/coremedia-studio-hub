package com.coremedia.blueprint.connectors.s7;

import com.coremedia.blueprint.connectors.s7.client.IpsApiClient;
import com.coremedia.cache.Cache;
import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.api.ConnectorException;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.search.ConnectorSearchResult;
import com.coremedia.connectors.caching.CacheableConnectorService;
import com.scene7.ipsapi.xsd._2013_02_15.Asset;
import com.scene7.ipsapi.xsd._2013_02_15.Folder;
import com.scene7.ipsapi.xsd._2013_02_15.FolderArray;
import com.scene7.ipsapi.xsd._2013_02_15.GetFolderTreeReturn;
import com.scene7.ipsapi.xsd._2013_02_15.SearchAssetsReturn;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.connectors.impl.ConnectorPropertyNames.DISPLAY_NAME;
import static com.coremedia.connectors.impl.ConnectorPropertyNames.FOLDER;

public class S7ConnectorServiceImpl extends CacheableConnectorService<S7Container> {

  public static final String ROOT_PATH = "/";
  public static final String ROOT = "root";
  private static final Logger LOGGER = LoggerFactory.getLogger(S7ConnectorServiceImpl.class);
  Map<String, List<S7Container>> itemsMap = new HashMap<>();
  Map<String, S7Container> filesMap = new HashMap<>();
  private String rootFolder = "CoreMedia";
  private String rootFolderId = rootFolder + ROOT_PATH;
  private com.coremedia.blueprint.connectors.s7.S7ConnectorCategory rootCategory;

  private IpsApiClient client;

  public S7ConnectorServiceImpl(Cache cache) {
    super(cache);
  }

  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
    super.init(context);
    String companyHandle = context.getProperty("companyHandle");
    if (StringUtils.isBlank(companyHandle)) {
      throw new ConnectorException("companyHandle property is not set");
    }
    String userid = context.getProperty("userid");
    if (StringUtils.isBlank(userid)) {
      throw new ConnectorException("Scene 7 userid property is not set");
    }
    String password = context.getProperty("password");
    if (StringUtils.isBlank(password)) {
      throw new ConnectorException("Scene 7 password property is not set");
    }
    String s7Url = context.getProperty("url");
    if (StringUtils.isBlank(s7Url)) {
      throw new ConnectorException("Scene 7 url property is not set");
    }

    String root = context.getProperty("rootFolder");
    if (StringUtils.isBlank(root)) {
      throw new ConnectorException("Scene 7 rootFolder property is not set");
    }
    rootFolder = root;

    client = new IpsApiClient(companyHandle, userid, password, s7Url);
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext connectorContext, @NonNull ConnectorId connectorId) throws ConnectorException {
    ConnectorId parentFolderId = getCategoryId(connectorId);
    ConnectorCategory category = getCategory(connectorContext, parentFolderId);
    S7Container file = filesMap.get(connectorId.getExternalId());
    return new S7ConnectorItem(category, context, connectorId, file);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext connectorContext, @NonNull ConnectorId connectorId) throws ConnectorException {
    ConnectorCategory parentCategory = getParentCategory(connectorContext, connectorId);
    if (parentCategory == null) {
      return getRootCategory(connectorContext);
    }

    S7Container file = getCachedItemOrCategoryEntity(connectorContext, connectorId);
    S7ConnectorCategory subCategory = new S7ConnectorCategory(parentCategory, context, connectorId, file);
    subCategory.setItems(getItems(connectorContext, subCategory));
    subCategory.setSubCategories(getSubCategories(connectorContext, subCategory));
    return subCategory;
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext connectorContext) throws ConnectorException {
    LOGGER.info("Loading root category");
    if (rootCategory == null) {
      String rootPath = context.getProperty(FOLDER);
      String displayName = context.getProperty(DISPLAY_NAME);

      if (StringUtils.isEmpty(displayName)) {
        displayName = ROOT;
      }

      if (rootPath == null) {
        rootPath = ROOT_PATH;
      }

      S7Container root = new S7Container();
      Folder folder = new Folder();
      folder.setPath(rootPath);
      root.setFolder(folder);
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new S7ConnectorCategory(null, context, id, root);
      rootCategory.setName(displayName);

      List<ConnectorCategory> subCategories = getSubCategories(connectorContext, rootCategory);
      rootCategory.setSubCategories(subCategories);

      List<ConnectorItem> items = getItems(connectorContext, rootCategory);
      rootCategory.setItems(items);
    }
    return rootCategory;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext connectorContext, ConnectorCategory connectorCategory, String s, String s1, Map<String, String> map) {
    List<ConnectorEntity> results = new ArrayList<>();
    //TODO: Implement search

    return new ConnectorSearchResult<>(results);
  }

  @Nullable
  public ConnectorItem upload(@NonNull ConnectorContext connectorContext, ConnectorCategory connectorCategory, String s, InputStream inputStream) {
    return null;
  }

  @Override
  public List<S7Container> list(ConnectorId categoryId) {
    String path = categoryId.getExternalId();
    if (categoryId.isRootId()) {
      path = context.getProperty(FOLDER);
    }
    try {
      String folderPath = path;
      if (!ROOT_PATH.equals(folderPath)) {
        folderPath = ROOT_PATH + folderPath;
      }
      LOGGER.info("Loading by path: {}", folderPath);
      List<S7Container> items = itemsMap.get(folderPath);
      if (items == null) {
        items = new ArrayList<>();
        itemsMap.put(folderPath, items);
      }
      else {
        return items;
      }
      GetFolderTreeReturn folderTree = client.getFolderTree(folderPath, 1);

      Folder folders = folderTree.getFolders();
      FolderArray subfolderArray = folders.getSubfolderArray();

      if (subfolderArray != null) {
        for (Folder folder : subfolderArray.getItems()) {
          S7Container container = new S7Container();
          container.setFolder(folder);
          items.add(container);
        }

      }
      if (!categoryId.isRootId()) {
        items.addAll(getS7Files(path));
      }
      return items;
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.info("Error", e);
    }
    return Collections.emptyList();
  }

  @Override
  public S7Container getEntity(ConnectorId id) {
    if (id.isRootId()) {
      return null;
    }

    S7Container container = new S7Container();
    Folder folder = new Folder();
    folder.setPath(id.getExternalId());
    container.setFolder(folder);
    return container;
  }

  @Override
  public boolean isItemEntity(S7Container entry) {
    return entry.getAsset() != null;
  }

  @Override
  public String getPath(S7Container entry) {
    String name = getName(entry);
    Asset asset = entry.getAsset();
    if (asset != null) {
      return asset.getFolder() + name;
    }
    return name;
  }

  @Override
  public String getName(S7Container entry) {
    return S7Helper.getName(entry);
  }

  private boolean isRootCategoryId(@NonNull ConnectorContext connectorContext, ConnectorId categoryId) {
    ConnectorId rootCategoryId = getRootCategory(connectorContext).getConnectorId();
    if (rootCategoryId.getId().equals(categoryId.getId())) {
      return true;
    }

    String categoryPath = categoryId.getExternalId();

    //check window specific path and remove drive letter
    if (categoryPath.indexOf(":") == 1) {
      categoryPath = categoryPath.substring(2, categoryPath.length());
    }

    //check for root
    String rootFolder = context.getProperty("folder");
    if (rootFolder == null) {
      rootFolder = ROOT_PATH;
    }

    rootFolder = rootFolder.replaceAll("\\\\", ROOT_PATH);

    if (categoryPath.equals("") || categoryPath.equals(rootFolder) || rootFolderId.equals(categoryPath)) {
      return true;
    }

    return false;
  }

  public ConnectorCategory getParentCategory(@NonNull ConnectorContext connectorContext, ConnectorId categoryId) {
    if (isRootCategoryId(connectorContext, categoryId)) {
      return null;
    }

    String categoryPath = categoryId.getExternalId();
    if (rootFolder.equals(categoryPath)) {
      return null;
    }
    String parentPath = categoryPath.substring(0, categoryPath.replaceAll("\\\\", ROOT_PATH).lastIndexOf(ROOT_PATH));

    //check for category path that end with a slash, e.g. S3
    if (categoryPath.endsWith(ROOT_PATH)) {
      //then we have to re-cut the path again
      if (!parentPath.contains(ROOT_PATH)) {
        parentPath = "";
      }
      else {
        parentPath = parentPath.substring(0, parentPath.replaceAll("\\\\", ROOT_PATH).lastIndexOf(ROOT_PATH) + 1);
      }
    }

    ConnectorId parentId = ConnectorId.createCategoryId(context.getConnectionId(), parentPath);
    return getCategory(connectorContext, parentId);

  }


  private List<ConnectorCategory> getSubCategories(@NonNull ConnectorContext connectorContext, @NonNull ConnectorCategory category) throws ConnectorException {
    List<ConnectorCategory> subCategories = new ArrayList<>();

    List<S7Container> subfolders = getSubcategoryEntities(connectorContext, category.getConnectorId());
    for (S7Container entry : subfolders) {
      ConnectorId connectorId = ConnectorId.createCategoryId(context.getConnectionId(), getPath(entry));
      S7ConnectorCategory subCategory = new S7ConnectorCategory(category, context, connectorId, entry);
      subCategory.setItems(getItems(connectorContext, subCategory));
      subCategories.add(subCategory);
    }

    return subCategories;
  }

  public List<S7Container> searchAssetsByMetadata(ConnectorId categoryId) {
    if (categoryId.isRootId()) {
      return Collections.emptyList();
    }
    String folder = categoryId.getExternalId();
    return getS7Files(folder);
  }

  private List<S7Container> getS7Files(String folder) {
    LOGGER.info("Searching files in folder: {}", folder);
    List<S7Container> items = itemsMap.get(folder);
    try {
      if (items == null) {
        items = new ArrayList<>();
        itemsMap.put(folder, items);
      }
      else {
        return items;
      }
      SearchAssetsReturn files = client.searchAssetsByMetadata(folder);
      List<Asset> assets = files.getAssetArray().getItems();
      LOGGER.info("{} files in folder: {}", assets.size(), folder);

      for (Asset asset : assets) {
        asset.setFolder(StringUtils.trimToEmpty(folder));
        S7Container container = new S7Container();
        container.setAsset(asset);
        items.add(container);
        filesMap.put(getPath(container), container);
      }
    } catch (Exception e) {
      LOGGER.error("Can't load files list", e);
    }
    return items;
  }


  private List<ConnectorItem> getItems(@NonNull ConnectorContext connectorContext, @NonNull ConnectorCategory category) throws ConnectorException {
    List<ConnectorItem> items = new ArrayList<>();

    List<S7Container> fileEntities = getItemEntities(connectorContext, category.getConnectorId());
    LOGGER.info("Found items by category: ({}) {}", fileEntities.size(), category.getConnectorId());
    for (S7Container entry : fileEntities) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), getPath(entry));
      S7ConnectorItem item = new S7ConnectorItem(category, context, itemId, entry);
      items.add(item);
    }

    return items;
  }

}
