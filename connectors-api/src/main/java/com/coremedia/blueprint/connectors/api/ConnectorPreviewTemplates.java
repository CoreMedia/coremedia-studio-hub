package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nullable;

/**
 * Encapsulated the preview template configuration.
 */
public interface ConnectorPreviewTemplates {

  /**
   * Returns the preview template for the given type
   * @param itemType the item type to retrieve the preview for
   * @return null if no template is provided
   */
  @Nullable
  String getTemplate(String itemType);
}
