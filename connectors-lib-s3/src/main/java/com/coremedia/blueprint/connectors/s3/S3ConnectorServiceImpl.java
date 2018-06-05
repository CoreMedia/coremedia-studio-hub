package com.coremedia.blueprint.connectors.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.filesystems.FileSystemItem;
import com.coremedia.blueprint.connectors.filesystems.FileBasedConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.ACCESS_KEY;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.BUCKET_NAME;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.DISPLAY_NAME;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.FOLDER;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.PROFILE;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.REGION;
import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.SECRET;

public class S3ConnectorServiceImpl extends FileBasedConnectorService<S3ObjectSummary> {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3ConnectorServiceImpl.class);

  private AmazonS3 s3Client;
  private S3ConnectorCategory rootCategory;

  @Override
  public boolean init(@Nonnull ConnectorContext context) {
    this.context = context;
    this.ensureSeparatorSuffix = true;

    String awsRegion = context.getProperty(REGION);
    String awsProfile = context.getProperty(PROFILE);
    String awsSecret = context.getProperty(SECRET);
    String awsAccessKey = context.getProperty(ACCESS_KEY);

    try {
      BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecret);
      s3Client = AmazonS3ClientBuilder.standard()
              .withRegion(awsRegion)
              .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
              .build();

      //execute a list call to see if the connection is working
      String bucketName = context.getProperty(BUCKET_NAME);
      s3Client.listObjects(bucketName);
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to create S3 client: " + e.getMessage() + ", used region '" + awsRegion + "' with profile '" + awsProfile + "'", e);
    }

    return false;
  }

  @Override
  public void shutdown(@Nonnull ConnectorContext context) throws ConnectorException {
    super.shutdown(context);
    if(s3Client != null) {
      s3Client.shutdown();
    }
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      String displayName = context.getProperty(DISPLAY_NAME);

      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new S3ConnectorCategory(this, null, context, null, id);
      rootCategory.setName(displayName);

      List<ConnectorCategory> subCategories = getSubCategories(context, rootCategory);
      rootCategory.setSubCategories(subCategories);

      List<ConnectorItem> items = getItems(context, rootCategory);
      rootCategory.setItems(items);
    }
    return rootCategory;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId itemId) throws ConnectorException {
    ConnectorId parentFolderId = getFolderId(itemId);
    S3ObjectSummary file = getCachedFileOrFolderEntity(context, itemId);
    return new S3ConnectorItem(this, getCategory(context, parentFolderId), context, file, itemId);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId categoryId) throws ConnectorException {
    ConnectorCategory parentCategory = getParentCategory(context, categoryId);
    if (parentCategory == null) {
      return getRootCategory(context);
    }

    S3ObjectSummary object = getCachedFileOrFolderEntity(context, categoryId);
    S3ConnectorCategory subCategory = new S3ConnectorCategory(this, parentCategory, context, object, categoryId);
    subCategory.setItems(getItems(context, subCategory));
    subCategory.setSubCategories(getSubCategories(context, subCategory));
    return subCategory;
  }

  public Boolean refresh(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category) {
    if(category.getConnectorId().isRootId()) {
      rootCategory = null;
      rootCategory = (S3ConnectorCategory) getRootCategory(context);
    }
    return super.refresh(context, category);
  }

  public ConnectorItem upload(@Nonnull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    try {
      String bucketName = context.getProperty(BUCKET_NAME);
      String uniqueObjectName = createUniqueFilename(context, category.getConnectorId(), itemName);
      //no leading slashes for s3
      if(uniqueObjectName.startsWith("/")) {
        uniqueObjectName = uniqueObjectName.substring(1, uniqueObjectName.length());
      }
      ConnectorId newItemId = ConnectorId.createItemId(context.getConnectionId(), uniqueObjectName);

      File tmpFile = File.createTempFile(itemName, "s3");
      tmpFile.deleteOnExit();
      OutputStream out = new FileOutputStream(tmpFile);
      IOUtils.copy(inputStream, out);
      inputStream.close();
      out.close();

      ObjectMetadata metaData = new ObjectMetadata();
      metaData.setContentLength(tmpFile.length());

      FileInputStream fileInputStream = new FileInputStream(tmpFile);
      String resourceId = newItemId.getExternalId();
      PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, resourceId, fileInputStream, metaData);
      s3Client.putObject(putObjectRequest);
      fileInputStream.close();

      boolean delete = tmpFile.delete();
      if(!delete) {
        throw new ConnectorException("Failed to delete s3 upload temp file");
      }

      refresh(context, category);
      return getItem(context, newItemId);
    } catch (IOException e) {
      LOGGER.error("Failed to upload " + itemName + ": " + e.getMessage(), e);
      throw new ConnectorException(e);
    }
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();
    if(query.equals("*")) {
      query = "";
    }

    if(searchType == null && query.equals("")) {
      results.addAll(getSubCategories(context, category));
      results.addAll(getItems(context, category));
    }
    else {
      FileSystemItem<S3ObjectSummary> cacheItem = listCachedEntities(context, category.getConnectorId());
      List<S3ObjectSummary> categoryList = cacheItem.getFolderItemsData();
      for (S3ObjectSummary objectSummary : categoryList) {
        if (!isFile(objectSummary)) {
          ConnectorId id = ConnectorId.createCategoryId(context.getConnectionId(), getPath(objectSummary));
          ConnectorCategory cat = getCategory(context, id);
          if ((searchType == null || searchType.equals(ConnectorCategory.DEFAULT_TYPE)) && cat.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
            results.add(cat);
          }
        }
        else {
          ConnectorId id = ConnectorId.createItemId(context.getConnectionId(), getPath(objectSummary));
          ConnectorItem item = getItem(context, id);
          if (item.isMatchingWithItemType(searchType) && item.getDisplayName().toLowerCase().contains(query.toLowerCase()) && item.getParent().getConnectorId().equals(category.getConnectorId())) {
            results.add(item);
          }
        }
      }
    }

    return new ConnectorSearchResult<>(results);
  }

  public boolean delete(S3ConnectorItem item) {
    String bucketName = item.getBucketName();
    String key = item.getKey();
    s3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
    refresh(this.context, rootCategory);
    return true;
  }

  public S3Object getS3Object(S3ConnectorEntity bean) {
    String key = bean.getKey();
    if (key != null) {
      try {
        return s3Client.getObject(bean.getBucketName(), bean.getKey());
      } catch (SdkClientException e) {
        LOGGER.error("Error trying to access S3 bucket: " + e.getMessage() + ", trying to re-establish connection...");
        s3Client.shutdown();
        init(context);

        return s3Client.getObject(bean.getBucketName(), bean.getKey());
      }
    }
    return null;
  }

  //---------------------------File System Connector -------------------------------------------------------------------

  public List<S3ObjectSummary> list(ConnectorId categoryId) {
    List<S3ObjectSummary> result = new ArrayList<>();
    String categoryPath = categoryId.getExternalId();
    if(categoryId.isRootId()) {
      categoryPath = context.getProperty(FOLDER);
      if(categoryPath == null) {
        categoryPath = "";
      }
    }

    String bucketName = context.getProperty(BUCKET_NAME);
    ObjectListing objectListing = s3Client.listObjects(bucketName);
    List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
    for (S3ObjectSummary objectSummary : objectSummaries) {
     if(S3Helper.isItemOf(objectSummary, categoryPath)) {
       result.add(objectSummary);
     }
     else if(S3Helper.isSubCategoryOf(objectSummary, categoryPath)) {
        result.add(objectSummary);
      }
    }

    return result;
  }

  public S3ObjectSummary getFile(ConnectorId id) {
    String searchPath = id.getExternalId();

    String bucketName = context.getProperty(BUCKET_NAME);
    ObjectListing objectListing = s3Client.listObjects(bucketName);
    List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
    for (S3ObjectSummary objectSummary : objectSummaries) {
      String path = objectSummary.getKey();
      if(searchPath.equals(path)) {
        return objectSummary;
      }
    }
    return null;
  }

  public boolean isFile(S3ObjectSummary object) {
    return !object.getKey().endsWith("/");
  }

  public String getName(S3ObjectSummary object) {
    return S3Helper.formatName(object);
  }

  public String getPath(S3ObjectSummary object) {
    return object.getKey();
  }

  //----------------------------- Helper -------------------------------------------------------------------------------

  private List<ConnectorCategory> getSubCategories(ConnectorContext context, @Nonnull ConnectorCategory category) throws ConnectorException {
    List<ConnectorCategory> subCategories = new ArrayList<>();

    List<S3ObjectSummary> subfolders = getSubfolderEntities(context, category.getConnectorId());
    for (S3ObjectSummary entry : subfolders) {
      ConnectorId connectorId = ConnectorId.createCategoryId(context.getConnectionId(), getPath(entry));
      S3ConnectorCategory subCategory = new S3ConnectorCategory(this, category, context, entry, connectorId);
      subCategory.setItems(getItems(context, subCategory));
      subCategories.add(subCategory);
    }

    return subCategories;
  }

  private List<ConnectorItem> getItems(ConnectorContext context, @Nonnull ConnectorCategory category) throws ConnectorException {
    List<ConnectorItem> items = new ArrayList<>();

    List<S3ObjectSummary> fileEntities = getFileEntities(context, category.getConnectorId());
    for (S3ObjectSummary entry : fileEntities) {
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), getPath(entry));
      S3ConnectorItem item = new S3ConnectorItem(this, category, context, entry, itemId);
      items.add(item);
    }

    return items;
  }
}
