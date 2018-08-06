package com.coremedia.blueprint.connectors.api.invalidation;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The result of an invalidation of a connector.
 */
public class InvalidationResult {
  private List<ConnectorEntity> entities = new ArrayList<>();
  private ConnectorContext context;

  private List<InvalidationMessage> messages = new ArrayList<>();

  public InvalidationResult(ConnectorContext context) {
    this.context = context;
  }

  public void addEntity(ConnectorEntity entity) {
    this.entities.add(entity);
  }

  /**
   * Creates a new invalidation message with the given parameters.
   * @param key the message key that is used to display the notification.
   * @param entity the entity that should be shown when the message is clicked
   * @param values the optional values that are used to render the message.
   */
  public void addMessage(@NonNull String key, @NonNull ConnectorEntity entity, List<Object> values) {
    this.messages.add(new InvalidationMessage(entity, key, values));
  }

  public List<ConnectorEntity> getEntities() {
    return entities;
  }

  public List<InvalidationMessage> getMessages() {
    return messages;
  }

  public ConnectorContext getContext() {
    return context;
  }

  @Override
  public String toString() {
    return "Invalidation Result of '" + context.getConnectionId() + "'";
  }
}
