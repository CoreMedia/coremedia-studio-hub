package com.coremedia.blueprint.connectors.s3;


import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class S3ConnectorItem extends S3ConnectorEntity implements ConnectorItem {

  S3ConnectorItem(S3ConnectorServiceImpl s3Service, ConnectorCategory parent, ConnectorContext context, S3ObjectSummary summary, ConnectorId connectorId) {
    super(s3Service, parent, context, summary, connectorId);
  }

  @Override
  public long getSize() {
    return s3ObjectSummary.getSize();
  }

  @Nullable
  @Override
  public String getDescription() {
    return "S3 Object '" + s3ObjectSummary.getKey() + "'";
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    return () -> {
      Map<String, Object> metaData = new HashMap<>();
      if(s3ObjectSummary.getOwner() != null) {
        metaData.put("owner", s3ObjectSummary.getOwner().getDisplayName());
      }

      metaData.put("storageClass", s3ObjectSummary.getStorageClass());

      ObjectMetadata objectMetadata = s3Service.getS3Object(this).getObjectMetadata();
      metaData.put("expirationDate", objectMetadata.getExpirationTime());
      metaData.put("cacheControl", objectMetadata.getCacheControl());
      metaData.putAll(objectMetadata.getUserMetadata());
      return metaData;
    };
  }

  @Override
  public boolean isDownloadable() {
    return true;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return s3Service.getS3Object(this).getObjectContent();
  }
}
