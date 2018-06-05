package com.coremedia.blueprint.connectors.canto.rest.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The Asset service offers operations that work with the original assets.
 * <ul>
 * <li>importing</li>
 * <li>downloading</li>
 * <li>deleting</li>
 * </ul> an asset.
 */
@Component("cantoAssetService")
public class AssetService extends AbstractCantoService {

  /**
   * Download asset data as {@link InputStream}.
   *
   * @param catalogId catalog
   * @param assetId   numeric asset id
   * @return {@link InputStream} for the asset data
   */
  public InputStream streamAsset(String catalogId, int assetId) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);
    pathParams.put(ASSET, Integer.toString(assetId));

    return connector.streamResource("/asset/download/{catalog}/{asset}", pathParams, null);
  }

  /**
   * Delete the asset with the given id from the catalog.
   *
   * @param catalogId catalog
   * @param assetId   numeric asset id
   * @return <code>true</code> if asset has been deleted <code>false</code> otherwise
   */
  public boolean deleteAsset(String catalogId, int assetId) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);
    pathParams.put(ASSET, Integer.toString(assetId));

    try {
      connector.performPost("/asset/delete/{catalog}/{asset}", pathParams, null, String.class);
      return true;
    } catch (HttpStatusCodeException e) {
      return false;
    }

  }

  /**
   * Upload an asset with data from the provided {@link InputStream} with the given name to the given catalog.
   *
   * @param catalogId   catalog
   * @param categoryId  category id
   * @param assetName   file name
   * @param inputStream data stream
   * @return numeric id if the created asset or <code>-1</code> if the asset could not be created.
   */
  public int uploadAsset(String catalogId, int categoryId, String assetName, InputStream inputStream) {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put(CATALOG, catalogId);

    Map<String, Object> metadata = null;
    if (categoryId > -1) {
      metadata = new HashMap<>();
      Map<String, Object> catAssignment = new HashMap<>();
      catAssignment.put("id", categoryId);
      metadata.put("Categories", Arrays.asList(catAssignment));
    }

    return connector.uploadResource("/asset/import/{catalog}", pathParams, null, assetName, metadata, inputStream);
  }

}
