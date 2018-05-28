package com.coremedia.blueprint.connectors.cloudinary;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryAsset;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryFolder;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.ContentType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component("cloudinaryService")
public class CloudinaryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryService.class);

  private CloudinaryConnector cloudinaryConnector;

  public void setConnector(CloudinaryConnector connector) {
    this.cloudinaryConnector = connector;
  }

  //---------------- Service Methods -----------------------------------------------------------------------------------

  @Cacheable(value = "cloudinaryFolderCache", key = "#context.connectionId + '_root'", cacheManager = "cacheManagerCloudinary")
  public List<CloudinaryFolder> getRootFolders(ConnectorContext context) {
    return cloudinaryConnector.getRootFolders();
  }

  @Cacheable(value = "cloudinaryAssetCache", key = "#context.connectionId + '_' + #path", cacheManager = "cacheManagerCloudinary")
  public CloudinaryAsset getAsset(ConnectorContext context, String path) {
    return cloudinaryConnector.getAsset(path);
  }

  @Cacheable(value = "cloudinaryAssetCache", key = "#context.connectionId + '_' + #path", cacheManager = "cacheManagerCloudinary")
  public List<CloudinaryAsset> getAssets(ConnectorContext context, String path) {
    return cloudinaryConnector.getAssets(path, true);
  }

  @Cacheable(value = "cloudinaryAssetCache", key = "#context.connectionId + '_all'", cacheManager = "cacheManagerCloudinary")
  public List<CloudinaryAsset> getAssets(ConnectorContext context) {
    return cloudinaryConnector.getAssets("", false);
  }

  @Cacheable(value = "cloudinaryFolderCache", key = "#context.connectionId + '_' + #folder", cacheManager = "cacheManagerCloudinary")
  public List<CloudinaryFolder> getSubfolders(ConnectorContext context, String folder) {
    return cloudinaryConnector.getSubFolders(folder);
  }

  public List<CloudinaryAsset> search(ConnectorContext context, String folder, String query, String searchType) {
    return cloudinaryConnector.search(folder, query, searchType);
  }

  public CloudinaryAsset upload(String folder, String itemName, InputStream inputStream) {
    return cloudinaryConnector.upload(folder, itemName, inputStream);
  }

  public Boolean delete(CloudinaryAsset asset) {
    return cloudinaryConnector.delete(asset);
  }

  public CloudinaryFolder getFolder(String externalId) {
    String path = externalId;
    String name = externalId;
    if(externalId.contains("/")) {
      name = externalId.substring(externalId.lastIndexOf('/')+1, externalId.length());
    }
    return new CloudinaryFolder(path, name);
  }

  public InputStream stream(CloudinaryAsset asset) {
    String url = asset.getUrl();
    try {
      HttpGet httpGet = new HttpGet(url);
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(httpGet);
      org.apache.http.HttpEntity ent = response.getEntity();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode > 200) {
        String result = IOUtils.toString(ent.getContent(), "utf8");
        LOGGER.error("Error getting Cloudinary resource: " + result);
      }
      else {
        return ent.getContent();
      }
    } catch (Exception e) {
      LOGGER.error("Couldn't connect to resource " + url + ": " + e.getMessage(), e);
    }
    return null;
  }
}