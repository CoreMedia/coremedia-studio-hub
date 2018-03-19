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
   * Optional additional metadata that will be used for the metadata-preview-panel in the library.
   * @return the metadata of the item or null if no additional metadata is available.
   */
  @Nullable
  ConnectorMetaData getMetaData();

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
   * Optional status field for connector items.
   * The value may be used to fill the additional 'status' column in the library
   * @return null or a status string
   */
  @Nullable
  default String getStatus() {
    return null;
  }
}
