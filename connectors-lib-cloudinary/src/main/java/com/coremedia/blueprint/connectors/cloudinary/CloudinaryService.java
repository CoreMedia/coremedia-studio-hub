package com.coremedia.blueprint.connectors.cloudinary;

import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryAsset;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryFolder;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 *
 */
public class CloudinaryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryService.class);

  private CloudinaryConnector cloudinaryConnector;

  public void setConnector(CloudinaryConnector connector) {
    this.cloudinaryConnector = connector;
  }

  //---------------- Service Methods -----------------------------------------------------------------------------------

  public List<CloudinaryFolder> getRootFolders(ConnectorContext context) {
    LOGGER.info("Cloudinary: requesting root folders");
    return cloudinaryConnector.getRootFolders();
  }

  public CloudinaryAsset getAsset(ConnectorContext context, String path) {
    LOGGER.info("Cloudinary: requesting asset " + path);
    return cloudinaryConnector.getAsset(path);
  }

  public List<CloudinaryAsset> getAssets(ConnectorContext context, String path) {
    LOGGER.info("Cloudinary: requesting assets " + path);
    return cloudinaryConnector.getAssets(path, true);
  }

  public List<CloudinaryAsset> getAssets(ConnectorContext context) {
    return cloudinaryConnector.getAssets("", false);
  }

  public List<CloudinaryFolder> getSubfolders(ConnectorContext context, String folder) {
    LOGGER.info("Cloudinary: requesting subfolders of " + folder);
    return cloudinaryConnector.getSubFolders(folder);
  }

  public List<CloudinaryAsset> search(ConnectorContext context, String folder, String query, String searchType) {
    LOGGER.info("Cloudinary: requesting search for folder " + folder);
    return cloudinaryConnector.search(folder, query, searchType);
  }

  public CloudinaryAsset upload(String folder, String itemName, InputStream inputStream) {
    LOGGER.info("Cloudinary: uploading to " + folder);
    return cloudinaryConnector.upload(folder, itemName, inputStream);
  }

  public boolean delete(CloudinaryAsset asset) {
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
