package com.coremedia.blueprint.connectors.shutterstock;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.shutterstock.rest.Category;
import com.coremedia.blueprint.connectors.shutterstock.rest.Picture;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.DISPLAY_NAME;

public class ShutterstockConnectorServiceImpl implements ConnectorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShutterstockConnectorServiceImpl.class);
  private static final String CACHE_MANAGER = "cacheManagerShutterstock";

  private ShutterstockConnectorCategory rootCategory;
  private ShutterstockService shutterstockService;

  public ShutterstockConnectorServiceImpl(ShutterstockService shutterstockService) {
    this.shutterstockService = shutterstockService;
  }

  @Override
  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
    try {
      String clientId = context.getProperty("clientId");
      String clientSecret = context.getProperty("clientSecret");

      if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
        throw new IllegalArgumentException("No clientId or clientSecret set for Shutterstock");
      }

      ShutterstockConnector shutterstockConnector = new ShutterstockConnector(clientId, clientSecret);
      shutterstockService.setConnector(shutterstockConnector);
    } catch (Exception e) {
      throw new ConnectorException(e);
    }
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    ConnectorId parentId = id.getParentId();
    Picture picture = shutterstockService.getPicture(id.getExternalId());
    Category parentCategory = picture.getCategories().get(0);
    ShutterstockConnectorCategory category = new ShutterstockConnectorCategory(rootCategory, context, parentId, parentCategory, parentCategory.getName());
    return new ShutterstockConnectorPicture(this, category, context, id, picture);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    if (id.isRootId()) {
      return rootCategory;
    }

    String externalId = id.getExternalId();
    Category category = shutterstockService.getCategory(externalId);
    ConnectorId categoryId = ConnectorId.createCategoryId(id.getConnectionId(), category.getId());
    return new ShutterstockConnectorCategory(rootCategory, context, categoryId, category, category.getName());
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) {
    if (rootCategory == null) {
      String displayName = context.getProperty(DISPLAY_NAME);
      if (StringUtils.isEmpty(displayName)) {
        displayName = "Shutterstock Pictures";
      }
      ConnectorId rootId = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new ShutterstockConnectorCategory(null, context, rootId, null, displayName);
      rootCategory.setSubCategories(getSubCategories(context));
      rootCategory.setItems(Collections.emptyList());
    }
    return rootCategory;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> result = new ArrayList<>();
    query = query.replaceAll("\\*", "");

    try {
      String categoryId = "";
      if (!category.getConnectorId().isRootId()) {
        categoryId = category.getConnectorId().getExternalId();
      }

      List<Picture> searchResult = shutterstockService.getPicturesByCategoriesSearch(categoryId, query);
      for (Picture picture : searchResult) {
        ConnectorId id = ConnectorId.createItemId(category.getConnectorId(), picture.getId());
        ConnectorItem item = getItem(context, id);
        result.add(item);
      }
    } catch (Exception e) {
      LOGGER.warn("Error retrieving shutterstock data", e.getMessage(), e);
    }
    return new ConnectorSearchResult<>(result);
  }

  public boolean refresh(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    CacheManager cacheManager = CacheManager.getCacheManager(CACHE_MANAGER);
    cacheManager.getCache("shutterstockCategoriesCache").removeAll();
    cacheManager.getCache("shutterstockPicturesByCategoryCache").removeAll();
    cacheManager.getCache("shutterstockPictureCache").removeAll();
    cacheManager.getCache("shutterstockCategoryCache").removeAll();
    cacheManager.getCache("shutterstockPicturesByCategorySearch").removeAll();
    init(context);
    return true;
  }

  // -------------------- Helper ---------------------------------------------------------------------------------------

  /**
   * There are only one kind of subcategories which are the playlists that are children
   * of the root channel, so we don't have to care about the tree relation here
   */
  private List<ConnectorCategory> getSubCategories(@NonNull ConnectorContext context) {
    List<ConnectorCategory> result = new ArrayList<>();
    List<Category> categories = shutterstockService.getCategories();
    for (Category category : categories) {
      ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), category.getId());
      ShutterstockConnectorCategory cat = new ShutterstockConnectorCategory(rootCategory, context, categoryId, category, category.getName());
      result.add(cat);
    }
    return result;
  }
}
