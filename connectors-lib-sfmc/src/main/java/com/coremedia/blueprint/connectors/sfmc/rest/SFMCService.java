package com.coremedia.blueprint.connectors.sfmc.rest;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCAsset;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCAssetCollection;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCCategory;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCCategoryCollection;
import com.coremedia.blueprint.connectors.sfmc.rest.documents.SFMCFileUpload;
import com.coremedia.blueprint.connectors.sfmc.rest.search.QueryBuilder;
import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 *
 */
public class SFMCService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SFMCService.class);

  private final static String REST_URL = ".rest.marketingcloudapis.com";
  private final static String ASSETS = REST_URL + "/asset/v1/content/assets";
  private final static String CATEGORIES = REST_URL + "/asset/v1/content/categories";

  private SFMCConnector connector;
  private Optional<SFMCCategoryCollection> categories;

  public void invalidate() {

  }

  public SFMCService(@NonNull SFMCConnector connector) {
    this.connector = connector;
  }

  public Optional<SFMCCategoryCollection> getCategories(@NonNull ConnectorContext context) {
    if (categories == null) { //TODO add caching
      categories = connector.getResource(CATEGORIES, SFMCCategoryCollection.class, context);
    }
    return categories;
  }

  public Optional<SFMCCategory> getCategory(@NonNull ConnectorContext context, int id) {
    return connector.getResource(CATEGORIES + "/" + id, SFMCCategory.class, context);
  }

  public Optional<SFMCAssetCollection> getAssets(@NonNull ConnectorContext context, int categoryId) {
    String query = QueryBuilder.createSimpleQuery("category.id", "equals", "int", categoryId).build();
    return connector.postResource(ASSETS + "/query", query, SFMCAssetCollection.class, context);
  }

  public Optional<SFMCAsset> getAsset(@NonNull ConnectorContext context, int id) {
    return connector.getResource(ASSETS + "/" + id, SFMCAsset.class, context);
  }

  public Optional<String> getThumbnailUrl(@NonNull ConnectorContext context,
                                          @NonNull SFMCAsset asset) {
    if (asset.getThumbnail() != null && asset.getThumbnail().getThumbnailUrl() != null) {
      String assetUrl = REST_URL + "/asset" + asset.getThumbnail().getThumbnailUrl();
      return Optional.of(assetUrl);
    }
    return Optional.empty();
  }

  public InputStream getBinaryPreview(@NonNull ConnectorContext context, int id) {
    try {
      Optional<SFMCAsset> asset = getAsset(context, id);
      String url = getThumbnailUrl(context, asset.get()).get();
      url = connector.resolveUrl(context, url);

      InputStream inputStream = connector.streamUrl(url);
      if (inputStream != null) {
        byte[] base64Bytes = IOUtils.toByteArray(inputStream);
        String base64 = new String(base64Bytes);
        base64 = base64.replaceAll("\"", "");
        byte[] bytes = new BASE64Decoder().decodeBuffer(base64);

        return new ByteArrayInputStream(bytes);
      }

    } catch (IOException e) {
      LOGGER.error("Failed to load binary preview thumbnail: " + e.getMessage(), e);
    }
    return null;
  }

  public InputStream getBinary(@NonNull ConnectorContext context, int id) {
    try {
      LOGGER.debug("Reading asset: " + id);
      Optional<String> resource = connector.getResource(ASSETS + "/" + id + "/file", String.class, context);
      if (!resource.isPresent()) {
        LOGGER.info("No asset data available for : " + id);
        return null;
      }

      String base64 = resource.get();
      base64 = base64.replaceAll("\"", "");
      byte[] bytes = new BASE64Decoder().decodeBuffer(base64);
      return new ByteArrayInputStream(bytes);
    } catch (IOException e) {
      LOGGER.error("Failed to load binary asset: " + e.getMessage(), e);
      return null;
    }
  }

  public Optional<SFMCAsset> upload(@NonNull SFMCCategory category,
                                    @NonNull ConnectorContext context,
                                    @NonNull String itemName,
                                    @NonNull InputStream inputStream) {
    try {
      byte[] bytes = IOUtils.toByteArray(inputStream);
      String base64String = new BASE64Encoder().encode(bytes);

      int assetTypeId = AssetMapping.getAssetId(itemName);
      String assetName = AssetMapping.getAssetName(itemName);

      SFMCFileUpload upload = new SFMCFileUpload(category, itemName, assetTypeId, assetName, base64String);
      String json = new Gson().toJson(upload);
      return connector.postResource(ASSETS, json, SFMCAsset.class, context);
    } catch (IOException e) {
      LOGGER.error("Failed to upload asset '" + itemName + "': " + e.getMessage(), e);
    }

    return Optional.empty();
  }
}
