package com.coremedia.blueprint.connectors.api;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

  private String id;
  private String externalId;
  private String connectionId;

  private ConnectorId(String id, String connectionId, String externalId) {
    this.id = id;
    this.connectionId = connectionId;
    this.externalId = externalId;
  }

  @Nonnull
  public static ConnectorId createCategoryId(@Nonnull String connectionId, @Nonnull String externalId) {
    String connectorId = PREFIX + connectionId + "/" + CATEGORY + "/" + externalId;
    return new ConnectorId(connectorId, connectionId, externalId);
  }

  @Nonnull
  public static ConnectorId createItemId(@Nonnull String connectionId, @Nonnull String externalId) {
    String connectorId = PREFIX + connectionId + "/" + ITEM + "/" + externalId;
    return new ConnectorId(connectorId, connectionId, externalId);
  }

  @Nonnull
  public static ConnectorId toId(@Nonnull String id) {
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
    return externalId;
  }

  public boolean isRootId() {
    return externalId.equals(ROOT);
  }

  @Nonnull
  public static ConnectorId createRootId(@Nonnull String connectionId) {
    return new ConnectorId(PREFIX + connectionId + "/" + CATEGORY + "/" + ROOT, connectionId, ROOT);
  }

  /**
   * Used during REST serialization
   */
  public String toUri() {
    try {
      return URLEncoder.encode(URLEncoder.encode(id, "utf8"), "utf8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
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
  public boolean equals(Object obj) {
    return obj.toString().equals(this.toString());
  }

  @Override
  public String toString() {
    return id;
  }
}
