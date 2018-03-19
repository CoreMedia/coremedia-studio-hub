package com.coremedia.blueprint.connectors.api;

import java.util.Map;

/**
 * Contains the item types as described in the itemTypes document.
 * The list of this types is also used for the search type combo in the Studio.
 */
public interface ConnectorItemTypes {

  /**
   * Returns all mappings of the Content Item Types configuration.
   */
  Map<String, Object> getTypes();

  /**
   * Returns the item type for the given item name
   * @param itemName the name to determine the type for
   * @return the item type defined in the corresponding settings document
   */
  String getTypeForName(String itemName);
}
