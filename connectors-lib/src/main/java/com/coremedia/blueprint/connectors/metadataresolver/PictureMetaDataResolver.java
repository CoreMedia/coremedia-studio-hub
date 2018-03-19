package com.coremedia.blueprint.connectors.metadataresolver;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IPTC metadata resolving.
 */
public class PictureMetaDataResolver implements ConnectorMetaDataResolver {
  private static final Logger LOG = LoggerFactory.getLogger(PictureMetaDataResolver.class);

  private static final List<String> IPTC_WHITELIST = Arrays.asList("caption/abstract",
          "imageheight",
          "Imagewidth",
          "city",
          "headline",
          "signature",
          "languageidentifier",
          "profile date/time",
          "country/primarylocationcode",
          "supplementalcategory(s)");

  @Override
  public boolean include(ConnectorItem item) {
    return item.getItemType().equals("picture");
  }

  @Override
  public ConnectorMetaData resolveMetaData(ConnectorItem item, File itemTempFile) {
    final Map<String, Object> result = new HashMap<>();
    com.drew.metadata.Metadata metadata = null;
    try {
      metadata = ImageMetadataReader.readMetadata(itemTempFile);
    } catch (Exception e) {
      LOG.warn("Error processing image data of {}: {}", item, e.getMessage());
    }
    if (metadata != null) {
      for (Directory directory : metadata.getDirectories()) {
        for (Tag tag : directory.getTags()) {
          if (tag.getTagName() != null && !StringUtils.isEmpty(tag.getDescription())) {
            String name = tag.getTagName().toLowerCase();
            if (IPTC_WHITELIST.contains(name)) {
              result.put(tag.getTagName(), tag.getDescription());
            }
          }
        }
      }
    }
    return () -> result;
  }
}
