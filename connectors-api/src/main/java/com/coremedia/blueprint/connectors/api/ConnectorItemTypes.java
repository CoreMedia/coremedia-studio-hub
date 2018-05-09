package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Contains the item types as described in the itemTypes document.
 * The list of this types is also used for the search type combo in the Studio.
 */
public interface ConnectorItemTypes {

  /**
   * Returns all mappings of the Content Item Types configuration.
   */
  @Nonnull
  Map<String, Object> getTypes();

  /**
   * Returns the item type for the given item name
   * @param itemName the name to determine the type for
   * @return the item type defined in the corresponding settings document
   */
  @Nullable
  String getTypeForName(String itemName);

  /**
   * Returns the target content type for the given type
   * @param type the item type without leading prefix
   */
  @Nonnull
  List<String> getTypes(String type);
}
