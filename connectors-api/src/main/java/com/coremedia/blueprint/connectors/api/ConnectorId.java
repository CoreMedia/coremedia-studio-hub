package com.coremedia.blueprint.connectors.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * A connector id is the unique identifier of connector item.
 * It contains the external id and the connection id to identify the service
 * to process the matching category or item.
 */
public class ConnectorId {
  private final static String PREFIX = "connector:///";
  private final static String CATEGORY = "category";
  private final static String ITEM = "item";
  private final static String ROOT = "root";

  private final static String PARENT_ID_SEPARATOR = "|";

  private String id;
  private String externalId;
  private String connectionId;

  private ConnectorId(String id, String connectionId, String externalId) {
    this.id = id;
    this.connectionId = connectionId;
    this.externalId = externalId;
  }

  @NonNull
  public static ConnectorId createCategoryId(@NonNull String connectionId, @NonNull Object externalId) {
    String connectorId = PREFIX + connectionId + "/" + CATEGORY + "/" + externalId;
    return new ConnectorId(connectorId, connectionId, String.valueOf(externalId));
  }

  /**
   * Only use this method if you have the parent reference anyway.
   * Every ConnectorItem should now their parent. If the external API
   * the concrete connector implementation is based on, does not provide this information,
   * the
   * <pre>ConnectorId createItemId(@NonNull ConnectorId parentId, @NonNull Object externalId)</pre>
   * method should be used instead.
   * @param connectionId the connection id to create the ID for
   * @param externalId the external id used to identify the external API item
   */
  @NonNull
  public static ConnectorId createItemId(@NonNull String connectionId, @NonNull Object externalId) {
    String connectorId = PREFIX + connectionId + "/" + ITEM + "/" + externalId;
    return new ConnectorId(connectorId, connectionId, String.valueOf(externalId));
  }

  /**
   * This method should be used to store the parent id as part of the item it.
   * This way, it is easier to resolve the parent in the <pre>ConnectorService.getItem</pre> method.
   * @param parentId the parent category id the item belongs to
   * @param externalId the external id used to identify the external API item
   */
  @NonNull
  public static ConnectorId createItemId(@NonNull ConnectorId parentId, @NonNull Object externalId) {
    String parentExternalId = parentId.getExternalId();
    String fullExternalId = externalId + PARENT_ID_SEPARATOR + parentExternalId;
    String connectorId = PREFIX + parentId.getConnectionId() + "/" + ITEM + "/" + fullExternalId;
    return new ConnectorId(connectorId, parentId.getConnectionId(), fullExternalId);
  }

  /**
   * Extracts the parent if from an item id.
   * @return the parent id or null if this id wasn't generated with a parent id included.
   */
  @Nullable
  public ConnectorId getParentId() {
    if(externalId.contains(PARENT_ID_SEPARATOR)) {
      String parentId = externalId.split("\\" + PARENT_ID_SEPARATOR)[1];
      String idString = PREFIX + connectionId + "/"  + CATEGORY + "/" + parentId;
      return new ConnectorId(idString, connectionId, parentId);
    }
    return null;
  }

  @NonNull
  public static ConnectorId toId(@NonNull String id) {
    String externalSegment = id;
    for (int i = 0; i < 5; i++) {
      externalSegment = externalSegment.substring(externalSegment.indexOf("/") + 1, externalSegment.length());
    }
    String connectionId = id.split("/")[3];
    return new ConnectorId(id, connectionId, externalSegment);
  }

  public String getId() {
    return id;
  }

  public String getExternalId() {
    if(externalId.contains("|")) {
      return externalId.split("\\" + PARENT_ID_SEPARATOR)[0];
    }
    return externalId;
  }

  public boolean isRootId() {
    return externalId.equals(ROOT);
  }

  @NonNull
  public static ConnectorId createRootId(@NonNull String connectionId) {
    return new ConnectorId(PREFIX + connectionId + "/" + CATEGORY + "/" + ROOT, connectionId, ROOT);
  }

  /**
   * Used during REST serialization
   */
  public String toUri() {
    try {
      return URLEncoder.encode(URLEncoder.encode(id, "utf8"), "utf8");
    } catch (UnsupportedEncodingException e) {
      //no
    }
    return null;
  }

  public boolean isItemId() {
    return toString().startsWith(PREFIX + connectionId + "/" + ITEM + "/");
  }

  public String getConnectionId() {
    return connectionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectorId that = (ConnectorId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id;
  }
}
