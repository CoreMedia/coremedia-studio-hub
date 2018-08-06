package com.coremedia.blueprint.connectors.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Search;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.api.exceptions.RateLimited;
import com.cloudinary.utils.ObjectUtils;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryAsset;
import com.coremedia.blueprint.connectors.cloudinary.rest.CloudinaryFolder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CloudinaryConnector {
  private static final Logger LOG = LoggerFactory.getLogger(CloudinaryConnector.class);
  private Cloudinary cloudinary;

  public CloudinaryConnector(Cloudinary cloudinary) {
    this.cloudinary = cloudinary;
  }

  public List<CloudinaryFolder> getRootFolders() {
    List<CloudinaryFolder> result = new ArrayList<>();
    try {
      List<Map> folders = (List<Map>) cloudinary.api().rootFolders(getDefaultOptions()).get("folders");
      for (Map folder : folders) {
        result.add(new CloudinaryFolder(folder));
      }
    } catch (Exception e) {
      LOG.error("Error loading Cloudinary root folders: " + e.getMessage(), e);
    }
    return result;
  }

  public List<CloudinaryFolder> getSubFolders(String path) {
    List<CloudinaryFolder> result = new ArrayList<>();
    try {
      List<Map> folders = (List<Map>) cloudinary.api().subFolders(path, getDefaultOptions()).get("folders");
      for (Map folder : folders) {
        result.add(new CloudinaryFolder(folder));
      }
    } catch (Exception e) {
      LOG.error("Error loading Cloudinary subfolders: " + e.getMessage(), e);
    }
    return result;
  }

  public List<CloudinaryAsset> getAssets(String path, boolean filter) {
    List<CloudinaryAsset> result = new ArrayList<>();
    try {
      List<Map> collectedAssets = new ArrayList<>();
      List<Map> assets = (List<Map>) cloudinary.api().resources(ObjectUtils.asMap("type", "upload", "prefix", path, "resource_type", "image")).get("resources");
      collectedAssets.addAll(assets);
      assets = (List<Map>) cloudinary.api().resources(ObjectUtils.asMap("type", "upload", "prefix", path, "resource_type", "raw")).get("resources");
      collectedAssets.addAll(assets);
      assets = (List<Map>) cloudinary.api().resources(ObjectUtils.asMap("type", "upload", "prefix", path, "resource_type", "video")).get("resources");
      collectedAssets.addAll(assets);

      for (Map asset : collectedAssets) {
        CloudinaryAsset cAsset = new CloudinaryAsset(asset);

        //filter for direct children
        if(filter) {
          if (cAsset.isInFolder(path)) {
            result.add(cAsset);
          }
        }
        else {
          result.add(cAsset);
        }
      }
    }
    catch (RateLimited rle) {
      throw new ConnectorException(rle);
    }
    catch (Exception e) {
      LOG.error("Error loading Cloudinary assets: " + e.getMessage(), e);
    }
    return result;
  }

  public List<CloudinaryAsset> search(String folder, String query, String searchType) {
    List<CloudinaryAsset> result = new ArrayList<>();
    try {
      String expression = query;
      expression = " -folder=" + folder;
      Search search = cloudinary.search().expression(expression).maxResults(100);
      List<Map> resources = (List<Map>) search.execute().get("resources");
      for (Map asset : resources) {
        CloudinaryAsset cAsset = new CloudinaryAsset(asset);
        result.add(cAsset);
      }
    } catch (Exception e) {
      LOG.error("Failed to execute Cloudinary search: " + e.getMessage(), e);
    }

    return result;
  }

  public CloudinaryAsset getAsset(String externalId) {
    CloudinaryAsset asset = getAsset(externalId, "image");
    if (asset == null) {
      asset = getAsset(externalId, "video");
    }

    if (asset == null) {
      asset = getAsset(externalId, "raw");
    }

    return asset;
  }

  public CloudinaryAsset upload(String folder, String name, InputStream inputStream) {
    File tmpFile = null;
    try {
      tmpFile = File.createTempFile(name, ".tmp");
      FileOutputStream out = new FileOutputStream(tmpFile);
      IOUtils.copyLarge(inputStream, out);
      out.close();
      inputStream.close();

      String publicId = folder + "/" + FilenameUtils.getBaseName(name);
      Map<String, Object> options = getDefaultOptions();
      options.put("filename", name);
      options.put("public_id", publicId);
      options.put("resource_type", "auto");
      Map resource = cloudinary.uploader().upload(tmpFile, options);
      if (resource.containsKey("error")) {
        Map error = (Map) resource.get("error");
        String msg = (String) error.get("message");
        LOG.error("Failed to upload '" + name + "': " + msg);
        throw new ConnectorException("Failed to upload '" + name + "': " + msg);
      }
      return new CloudinaryAsset(resource);
    } catch (IOException e) {
      LOG.error("Error loading Cloudinary asset: " + e.getMessage(), e);
    } finally {
      if (tmpFile != null) {
        tmpFile.delete();
      }
    }
    return null;
  }

  public boolean delete(CloudinaryAsset asset) {
    try {
      Map<String, Object> options = getDefaultOptions();
      options.put("invalidate", true);
      options.put("resource_type", asset.getResourceType());
      Map result = cloudinary.api().deleteResources(Arrays.asList(asset.getId()), options);
      if (result.containsKey("error")) {
        Map error = (Map) result.get("error");
        String msg = (String) error.get("message");
        LOG.error("Failed to delete Cloudinary asset: " + msg);
        return false;
      }

      return true;
    } catch (Exception e) {
      LOG.error("Failed to delete Cloudinary asset: " + e.getMessage(), e);
    }
    return false;
  }
  //------------------------- Helper -----------------------------------------------------------------------------------

  private CloudinaryAsset getAsset(String externalId, String resourceType) {
    try {
      String id = URLEncoder.encode(externalId, "utf8");
      Map resource = cloudinary.api().resource(id, ObjectUtils.asMap("resource_type", resourceType));
      return new CloudinaryAsset(resource);
    } catch (NotFound nf) {
      LOG.info("Cloudinary asset " + externalId + " not found as resource type '" + resourceType + "'");
      return null;
    } catch (Exception e) {
      LOG.error("Error loading Cloudinary asset: " + e.getMessage(), e);
    }
    return null;
  }


  private Map getDefaultOptions() {
    Map<String, Object> options = new HashMap<>();
    options.put("return_error", true);
    return options;
  }
}
