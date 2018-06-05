package com.coremedia.blueprint.connectors.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryAsset;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryFolder;
import com.coremedia.cap.content.ContentType;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CloudinaryConnectorServiceImpl implements ConnectorService {
  private static final Logger LOG = LoggerFactory.getLogger(CloudinaryConnectorServiceImpl.class);
  private static final String CLOUDINARY_CACHE = "cacheManagerCloudinary";

  private static final String API_KEY = "apiKey";
  private static final String API_SECRET = "apiSecret";
  private static final String CLOUD_NAME = "cloudName";
  private static final String SEARCH_API_ENABLED = "searchApiEnabled";

  private CloudinaryConnectorCategory rootCategory;
  private CloudinaryService cloudinaryService;
  private boolean searchApiEnabled;

  @Resource(name = "cloudinaryService")
  public void setCloudinaryService(CloudinaryService cloudinaryService) {
    this.cloudinaryService = cloudinaryService;
  }


  @Override
  public boolean init(@Nonnull ConnectorContext context) throws ConnectorException {
    String apiKey = context.getProperty(API_KEY);
    String apiSecret = context.getProperty(API_SECRET);
    String cloudName = context.getProperty(CLOUD_NAME);

    if (apiKey == null || apiSecret == null || cloudName == null) {
      throw new ConnectorException("Invalid configuration for connector 'Cloudinary', ensure that apiKey, apiSecret and cloudName is set.");
    }

    this.searchApiEnabled = context.getBooleanProperty(SEARCH_API_ENABLED, false);

    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name",
            cloudName,
            "api_key",
            apiKey,
            "api_secret",
            apiSecret));

    this.cloudinaryService.setConnector(new CloudinaryConnector(cloudinary));
    LOG.info("Studio Hub Extension for Cloudinary initialized!");
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    String externalId = id.getExternalId();
    CloudinaryAsset asset = cloudinaryService.getAsset(context, externalId);
    if(asset != null) {
      String folder = asset.getFolder();
      ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), folder);
      CloudinaryConnectorCategory category = (CloudinaryConnectorCategory) getCategory(context, categoryId);
      return new CloudinaryConnectorItem(id, context, this, asset, category);
    }
    return null;
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    if (id.isRootId()) {
      return getRootCategory(context);
    }

    return createCategory(context, id);
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      String name = context.getProperty("displayName");
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      List<ConnectorCategory> children = new ArrayList<>();
      rootCategory = new CloudinaryConnectorCategory(this, context, id, name, children);

      List<CloudinaryFolder> rootNodes = cloudinaryService.getRootFolders(context);
      for (CloudinaryFolder node : rootNodes) {
        ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), node.getFolder());
        ConnectorCategory category = createCategory(context, categoryId);
        children.add(category);
      }
    }
    return rootCategory;
  }

  @Override
  public InvalidationResult invalidate(@Nonnull ConnectorContext context) {
    return null;
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    CloudinaryConnectorCategory cloudinaryCateggory = (CloudinaryConnectorCategory) category;
    List<ConnectorEntity> result = new ArrayList<>();

    String folder = "";
    if(cloudinaryCateggory.getFolder() != null) {
      folder= cloudinaryCateggory.getFolder().getFolder();
    }

    List<CloudinaryAsset> searchResults = new ArrayList<>();
    if(searchApiEnabled) {
      searchResults = cloudinaryService.search(context, folder, query, searchType);
    }
    else {
      String term = query.replace("*", "").toLowerCase();
      List<CloudinaryAsset> assets = cloudinaryService.getAssets(context);

      for (CloudinaryAsset asset : assets) {
        String name = asset.getName().toLowerCase();
        String itemType = asset.getConnectorItemType(context);

        boolean nameMatching = name.startsWith(term);
        boolean folderMatching = folder.equals("") || asset.isInFolder(folder);
        boolean typeMatching = searchType.equals(ContentType.CONTENT) || itemType.endsWith(searchType);

        if(nameMatching && folderMatching && typeMatching) {
          searchResults.add(asset);
        }
      }
    }

    for (CloudinaryAsset asset : searchResults) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), asset.getId());
      CloudinaryConnectorItem item = new CloudinaryConnectorItem(itemId, context, this, asset, null);
      result.add(item);
    }

    return new ConnectorSearchResult<>(result);
  }

  public Boolean refresh(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category) {
    CacheManager cacheManager = CacheManager.getCacheManager(CLOUDINARY_CACHE);
    cacheManager.getCache("cloudinaryFolderCache").removeAll();
    clearAssetCache();
    init(context);
    return true;
  }

  public ConnectorItem upload(@Nonnull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    CloudinaryConnectorCategory cloudinaryCategory = (CloudinaryConnectorCategory) category;
    String folder = "";
    if(cloudinaryCategory.getFolder() != null) {
      folder = cloudinaryCategory.getFolder().getFolder();
    }
    CloudinaryAsset asset = cloudinaryService.upload(folder, itemName, inputStream);
    if(asset != null) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), asset.getId());
      CloudinaryConnectorItem item = new CloudinaryConnectorItem(itemId, context, this, asset, (CloudinaryConnectorCategory) category);
      clearAssetCache();
      return item;
    }
    return null;
  }

  public InputStream stream(CloudinaryAsset asset) {
    return cloudinaryService.stream(asset);
  }

  public Boolean delete(ConnectorContext context, CloudinaryAsset asset) {
    Boolean result = cloudinaryService.delete(asset);
    clearAssetCache();
    CacheManager cacheManager = CacheManager.getCacheManager(CLOUDINARY_CACHE);
    cacheManager.getCache("cloudinaryFolderCache").removeAll();
    return result;
  }

  //------------------------- Helper -----------------------------------------------------------------------------------

  private void clearAssetCache() {
    CacheManager cacheManager = CacheManager.getCacheManager(CLOUDINARY_CACHE);
    cacheManager.getCache("cloudinaryAssetCache").removeAll();
  }

  private ConnectorCategory createCategory(ConnectorContext context, ConnectorId categoryId) {
    CloudinaryFolder category = cloudinaryService.getFolder(categoryId.getExternalId());
    return createCategory(context, categoryId, category);
  }

  private ConnectorCategory createCategory(ConnectorContext context, ConnectorId categoryId, CloudinaryFolder cloudinaryFolder) {
    List<ConnectorCategory> subCategories = new ArrayList<>();
    String parentFolder = cloudinaryFolder.getParentFolder();
    ConnectorCategory parentCategory = rootCategory;
    if (parentFolder != null) {
      ConnectorId connectorId = ConnectorId.createCategoryId(context.getConnectionId(), parentFolder);
      parentCategory = new CloudinaryConnectorCategory(this, context, connectorId, cloudinaryFolder, null, Collections.emptyList());
    }

    //add assets
    CloudinaryConnectorCategory category = new CloudinaryConnectorCategory(this, context, categoryId, cloudinaryFolder, parentCategory, subCategories);
    category.setChildItems(getAssets(context, category));

    //add sub categories
    List<CloudinaryFolder> folders = cloudinaryService.getSubfolders(context, cloudinaryFolder.getFolder());
    for (CloudinaryFolder subFolder: folders) {
      ConnectorId childCategoryId = ConnectorId.createCategoryId(context.getConnectionId(), subFolder.getFolder());
      CloudinaryConnectorCategory childCat = new CloudinaryConnectorCategory(this, context, childCategoryId, subFolder, category, Collections.emptyList());
      subCategories.add(childCat);
    }

    return category;
  }

  private List<ConnectorItem> getAssets(ConnectorContext context, CloudinaryConnectorCategory category) {
    List<ConnectorItem> items = new ArrayList<>();
    List<CloudinaryAsset> assets = cloudinaryService.getAssets(context, category.getFolder().getFolder());

    for (CloudinaryAsset asset : assets) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), asset.getId());
      CloudinaryConnectorItem item = new CloudinaryConnectorItem(itemId, context, this, asset, category);
      items.add(item);
    }

    return items;
  }
}
