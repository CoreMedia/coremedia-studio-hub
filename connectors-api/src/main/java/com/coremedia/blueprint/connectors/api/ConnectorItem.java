package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * Common interface to be implemented for documents.
 */
public interface ConnectorItem extends ConnectorEntity {

  String DEFAULT_TYPE = "default";

  /**
   * Returns the document size in bytes.
   */
  long getSize();

  /**
   * Returns the URL the item can be loaded from.
   * Note that his URL is the actual data URL and not
   * the URl that is used when the 'Show in connector system' action is used.
   */
  @Nullable
  default String getOpenInTabUrl() {
    ConnectorId id = getConnectorId();
    return "/api/connector/item/" + id.toUri() + "/data?mode=open";
  }

  /**
   * Returns the URL the item can be streamed from.
   */
  @Nullable
  default String getStreamUrl() {
    ConnectorId id = getConnectorId();
    return "/api/connector/item/" + id.toUri() + "/data?mode=stream";
  }

  /**
   * Returns the URL that is used to download the asset.
   */
  @Nullable
  default String getDownloadUrl() {
    ConnectorId id = getConnectorId();
    return "/api/connector/item/" + id.toUri() + "/data?mode=download";
  }

  @Nullable
  @Override
  default String getThumbnailUrl() {
    String type = getItemType();
    if(type.startsWith("picture")) {
      return getStreamUrl();
    }
    return null;
  }

  /**
   * The description of the item.
   * The returned String may contain HTML that can be used to render the preview.
   * @return the description of the item
   */
  @Nullable
  String getDescription();

  /**
   * Creates an input stream to the resource, e.g. an image on a filesystem.
   * @return an input stream or null if the resource is not stream-able.
   */
  @Nullable
  InputStream stream();

  /**
   * Returns true if the item is downloadable to the users disc.
   * If true, the download button in the library should is enabled.
   * @return true to enable item download
   */
  boolean isDownloadable();

  /**
   * This is the default implementation of the the item type calculation.
   * The Context is used to analyze the document name mapping with the item name.
   * @return the type of the item or null if the type could not be determined or not item types have been configured
   */
  @Nonnull
  default String getItemType() {
    ConnectorContext context = getContext();
    ConnectorItemTypes itemTypes = context.getItemTypes();
    if(itemTypes != null) {
      String typeForName = itemTypes.getTypeForName(getName());
      if(typeForName != null) {
        return typeForName;
      }
    }
    return DEFAULT_TYPE;
  }

  @Nullable
  default String getTargetContentType() {
    ConnectorContext context = getContext();
    ConnectorContentMappings contentMappings = context.getContentMappings();
    if(contentMappings != null) {
      return contentMappings.get(getItemType());
    }
    throw new UnsupportedOperationException("No content mappings found for " + this + ". " +
            "Either define a content mapping for '" + getItemType()+  "' or implement 'getTargetContentType()'");
  }

  /**
   * By default, the integration supports previews depending on the item type.
   * However, a custom preview snippet may be provided this way
   * @return the HTML used to preview this item
   */
  @Nullable
  default String getPreviewHtml() {
    ConnectorContext context = getContext();
    ConnectorPreviewTemplates previewTemplates = context.getPreviewTemplates();
    if(previewTemplates != null) {
      return previewTemplates.getTemplate(getItemType());
    }
    return null;
  }

  /**
   * Default implementation for type matching.
   * If no type is given then we assume that the item matches
   * @param type the type to check for or null
   * @return true if this item matches the given type
   */
  default boolean isMatchingWithItemType(@Nullable String type) {
    if(type == null) {
      return true;
    }
    if(type.equals(DEFAULT_TYPE)) {
      return true;
    }

    String itemType = getItemType();
    return itemType.equals(type);
  }

  /**
   * Default implementation for the mime type of a connector item.
   * Usually the mime type is detected automatically, using the item name.
   * This some system this information may not be available so the mime type has to be determined programmatically.
   * @return the mime type of the item data or null if the mime type should be detected automatically.
   */
  default String getMimeType() {
    return null;
  }
}
