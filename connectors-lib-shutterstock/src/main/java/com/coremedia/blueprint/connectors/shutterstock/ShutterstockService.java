package com.coremedia.blueprint.connectors.shutterstock;

import com.coremedia.blueprint.connectors.shutterstock.rest.Categories;
import com.coremedia.blueprint.connectors.shutterstock.rest.Category;
import com.coremedia.blueprint.connectors.shutterstock.rest.Picture;
import com.coremedia.blueprint.connectors.shutterstock.rest.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ShutterstockService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShutterstockService.class);

  private ShutterstockConnector shutterstockConnector;

  public void setConnector(ShutterstockConnector connector) {
    this.shutterstockConnector = connector;
  }

  //---------------- Service Methods -----------------------------------------------------------------------------------

  @Cacheable(value = "shutterstockCategoriesCache", key = "'categories'", cacheManager = "cacheManagerShutterstock")
  public List<Category> getCategories() {
    try {
      Categories categoriesList = shutterstockConnector.performGet("/images/categories", Categories.class);
      return categoriesList.getData();
    } catch (Exception e) {
      LOGGER.error("Failed to read shutterstock categories: {}" + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

//  @Cacheable(value = "shutterstockPicturesByCategoryCache", key = "'picturesByCategory_' + #categoryId", cacheManager = "cacheManagerShutterstock")
  public List<Picture> getPicturesByCategories(String categoryId) {
    try {
      MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
      queryParam.put("category", Arrays.asList(categoryId));
      SearchResult searchResult = shutterstockConnector.performGet("/images/search", null, queryParam, SearchResult.class);
      return searchResult.getData();
    } catch (Exception e) {
      LOGGER.error("Failed to read shutterstock categories: {}" + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Cacheable(value = "shutterstockPicturesByCategorySearch", key = "'picturesByCategory_' + #categoryId + '_' + #term", cacheManager = "cacheManagerShutterstock")
  public List<Picture> getPicturesByCategoriesSearch(String categoryId, String term) {
    try {
      MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
      if (StringUtils.isEmpty(categoryId)) {
        queryParam.put("category", Arrays.asList(categoryId));
      }
      queryParam.put("per_page", Arrays.asList("20"));
      queryParam.put("query", Arrays.asList(term));
      SearchResult searchResult = shutterstockConnector.performGet("/images/search", null, queryParam, SearchResult.class);
      return searchResult.getData();
    } catch (Exception e) {
      LOGGER.error("Failed to read shutterstock categories: {}" + e.getMessage(), e);
    }
    return Collections.emptyList();
  }


  @Cacheable(value = "shutterstockPictureCache", key = "'picture_' + #id", cacheManager = "cacheManagerShutterstock")
  public Picture getPicture(String id) {
    try {
      MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
      queryParam.put("id", Arrays.asList(id));
      queryParam.put("view", Arrays.asList("full"));
      SearchResult images = shutterstockConnector.performGet("/images", null, queryParam, SearchResult.class);
      if (!images.getData().isEmpty()) {
        return images.getData().get(0);
      }
    } catch (Exception e) {
      LOGGER.error("Failed to get picture {}: {}", id, e.getMessage(), e);
    }
    return null;
  }

  @Cacheable(value = "shutterstockCategoryCache", key = "'category_' + #id", cacheManager = "cacheManagerShutterstock")
  public Category getCategory(String id) {
    try {
      List<Category> categories = getCategories();
      for (Category category : categories) {
        if (category.getId().equals(id)) {
          return category;
        }
      }
    } catch (Exception e) {
      LOGGER.error("Failed to get category {}: {}", id, e.getMessage(), e);
    }
    return null;
  }
}
