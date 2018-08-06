package com.coremedia.blueprint.connectors.api;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;

import javax.activation.MimeType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Common interface to be implemented by category/folder objects of
 * the corresponding system.
 * A category consists of sub categories and child items.
 */
public interface ConnectorCategory extends ConnectorEntity {

  /**
   * This string is used during a search, e.g. when 'Folder' is selected
   * inside the library, this value is passed as search type.
   */
  String DEFAULT_TYPE = "folder";

  @NonNull
  List<ConnectorCategory> getSubCategories();

  /**
   * Returns a list of child items for this category.
   */
  @NonNull
  List<ConnectorItem> getItems();

  /**
   * Optional method that can be implemented to provide a custom column model inside the Studio library.
   * The given column models will be shown in the the order they are added to the list.
   */
  @NonNull
  default List<ConnectorColumn> getColumns() {
    return Collections.emptyList();
  }

  /**
   * Indicates if bulk uploads are supported.
   * @return true if uploads are enabled for this category
   */
  boolean isWriteable();

  /**
   * Returns true if the connector implementation is able
   * to deal with content items dropped on categories.
   */
  default boolean isContentUploadEnabled() {
    return isWriteable();
  }

  /**
   * Default interface for content drop.
   * The methods has to be implemented for categories that allow content drops
   * @param contents the contents that have been dropped on the category.
   * @param defaultAction true, if the default action was used (no CTRL was pressed)
   * @return true if the drop was successful
   */
  default boolean uploadContent(@NonNull ConnectorContext context, @NonNull List<Content> contents, Boolean defaultAction) {
    for (Content content : contents) {
      List<CapPropertyDescriptor> descriptors = content.getType().getDescriptors();
      for (CapPropertyDescriptor descriptor : descriptors) {
        if(descriptor.getType().equals(CapPropertyDescriptorType.BLOB)) {
          Blob blob = content.getBlob(descriptor.getName());
          if(blob != null && blob.getContentType() != null) {
            MimeType contentType = blob.getContentType();
            String name = content.getName() + "." + contentType.getSubType();
            return upload(context, name, blob.getInputStream()) != null;
          }
        }
      }
    }

    return false;
  }

  /**
   * Optional method to be overwritten when a custom label
   * should be applied for the a category in the library.
   *
   * @return the category type label suffix, will be looked up as category_type_<TYPE>_name inside the resource bundle
   */
  default String getType() {
    return DEFAULT_TYPE;
  }

  /**
   * Called to refresh the service
   * @return true if the operation was successful
   */
  default boolean refresh(@NonNull ConnectorContext context) {
    return true;
  }

  /**
   * Uses the given InputStream and name to create a new connector item in
   * the selected category or folder
   * @param itemName the name of the new connector item
   * @param inputStream the upload input stream
   * @return the newly created item created from the stream or null if not upload is provided
   */
  @Nullable
  default ConnectorItem upload(@NonNull ConnectorContext context, String itemName, InputStream inputStream) {
    return null;
  }
}
