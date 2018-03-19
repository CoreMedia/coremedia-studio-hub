package com.coremedia.blueprint.connectors.api.invalidation;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;

import java.util.List;

/**
 *
 */
public class InvalidationMessage {
  private String key;
  private ConnectorEntity entity;
  private List<Object> values;

  InvalidationMessage(ConnectorEntity entity, String key, List<Object> values) {
    this.entity = entity;
    this.values = values;
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public List<Object> getValues() {
    return values;
  }

  public ConnectorEntity getEntity() {
    return entity;
  }
}
