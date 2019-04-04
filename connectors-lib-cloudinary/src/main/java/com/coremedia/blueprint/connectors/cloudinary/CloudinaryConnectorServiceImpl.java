package com.coremedia.blueprint.connectors.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryAsset;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryEntity;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryFolder;
import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentType;
import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.api.ConnectorException;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.search.ConnectorSearchResult;
import com.coremedia.connectors.caching.CacheableConnectorService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CloudinaryConnectorServiceImpl extends CacheableConnectorService<CloudinaryEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(CloudinaryConnectorServiceImpl.class);
  private static final String CLOUDINARY_CACHE = "cacheManagerCloudinary";

  private static final String API_KEY = "apiKey";
  private static final String API_SECRET = "apiSecret";
  private static final String CLOUD_NAME = "cloudName";
  private static final String SEARCH_API_ENABLED = "searchApiEnabled";

  private CloudinaryConnectorCategory rootCategory;
  private CloudinaryService cloudinaryService;
  private boolean searchApiEnabled;

  protected CloudinaryConnectorServiceImpl(Cache cache) {
    super(cache);
  }

  @Resource(name = "cloudinaryService")
  public void setCloudinaryService(CloudinaryService cloudinaryService) {
    this.cloudinaryService = cloudinaryService;
  }


  @Override
  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
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
    return super.init(context);
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    CloudinaryAsset asset = (CloudinaryAsset) getCachedItemOrCategoryEntity(context, id);
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
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    if (id.isRootId()) {
      return getRootCategory(context);
    }

    return createCategory(context, id);
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
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

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
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

  public boolean refresh(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    if(category.getConnectorId().isRootId()) {
      rootCategory = null;
      rootCategory = (CloudinaryConnectorCategory) getRootCategory(context);
    }
    return super.refresh(context, category);
  }

  public ConnectorItem upload(@NonNull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    CloudinaryConnectorCategory cloudinaryCategory = (CloudinaryConnectorCategory) category;
    String folder = "";
    if(cloudinaryCategory.getFolder() != null) {
      folder = cloudinaryCategory.getFolder().getFolder();
    }
    CloudinaryAsset asset = cloudinaryService.upload(folder, itemName, inputStream);
    if(asset != null) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), asset.getId());
      CloudinaryConnectorItem item = new CloudinaryConnectorItem(itemId, context, this, asset, (CloudinaryConnectorCategory) category);
      refresh(context, category);
      return item;
    }
    return null;
  }

  public InputStream stream(CloudinaryAsset asset) {
    return cloudinaryService.stream(asset);
  }

  public boolean delete(ConnectorContext context, CloudinaryAsset asset) {
    boolean result = cloudinaryService.delete(asset);
    refresh(context, rootCategory);
    return result;
  }

  //------------------------- Caching-----------------------------------------------------------------------------------

  @Override
  public List<CloudinaryEntity> list(ConnectorId categoryId) {
    List<CloudinaryEntity> result = new ArrayList<>();
    CloudinaryFolder folder = cloudinaryService.getFolder(categoryId.getExternalId());

    List<CloudinaryFolder> folders = cloudinaryService.getSubfolders(context, folder.getFolder());
    result.addAll(folders);

    List<CloudinaryAsset> assets = cloudinaryService.getAssets(context, folder.getFolder());
    result.addAll(assets);

    return result;
  }

  @Override
  public CloudinaryEntity getEntity(ConnectorId id) {
    if(id.isItemId()) {
      return cloudinaryService.getAsset(context, id.getExternalId());
    }

    return cloudinaryService.getFolder(id.getExternalId());
  }

  @Override
  public boolean isItemEntity(CloudinaryEntity entry) {
    return !entry.isFolder();
  }

  @Override
  public String getPath(CloudinaryEntity entry) {
    return entry.getFolder();
  }

  @Override
  public String getName(CloudinaryEntity entry) {
    return entry.getName();
  }


  //------------------------- Helper -----------------------------------------------------------------------------------
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

    CloudinaryConnectorCategory category = new CloudinaryConnectorCategory(this, context, categoryId, cloudinaryFolder, parentCategory, subCategories);
    List<CloudinaryEntity> folders = getSubcategoryEntities(context, category.getConnectorId());
    for (CloudinaryEntity entity : folders) {
      CloudinaryFolder subFolder = (CloudinaryFolder) entity;
      ConnectorId childCategoryId = ConnectorId.createCategoryId(context.getConnectionId(), subFolder.getFolder());
      CloudinaryConnectorCategory childCat = new CloudinaryConnectorCategory(this, context, childCategoryId, subFolder, category, Collections.emptyList());
      subCategories.add(childCat);
    }

    List<CloudinaryEntity> entities = getItemEntities(context, category.getConnectorId());
    for (CloudinaryEntity entity : entities) {
      CloudinaryAsset asset = (CloudinaryAsset) entity;
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), asset.getId());
      CloudinaryConnectorItem item = new CloudinaryConnectorItem(itemId, context, this, asset, category);
      category.getItems().add(item);
    }

    return category;
  }
}
