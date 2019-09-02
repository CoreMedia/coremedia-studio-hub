package com.coremedia.blueprint.studio.connectors.rest.notifications;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationMessage;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.user.Group;
import com.coremedia.cap.user.User;
import com.coremedia.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The connector notification service is used to send various messages that
 * have been generated through the different connector implementations.
 */
public class ConnectorNotificationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorNotificationService.class);

  private final static String MESSAGE_TYPE_INVALIDATION = "invalidation";

  @Inject
  private NotificationService notificationService;

  private ContentRepository contentRepository;

  public ConnectorNotificationService(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  /**
   * Sends the notifications that have been created during
   * the connector service invalidation.
   */
  public void sendInvalidationNotification(InvalidationResult result) {
    if (result.getMessages().isEmpty()) {
      return;
    }

    Collection<User> notificationUsers = getNotificationUsers(result.getContext());
    int count = 0;
    for (User memberUser : notificationUsers) {
      List<InvalidationMessage> messages = result.getMessages();
      for (InvalidationMessage message : messages) {
        count++;
        List<Object> values = new ArrayList<>();
        values.add(message.getEntity().getConnectorId().toString());
        if (message.getValues() != null) {
          values.addAll(message.getValues());
        }
        notificationService.createNotification(MESSAGE_TYPE_INVALIDATION, memberUser, message.getKey(), values);
      }
    }
    LOGGER.info("ConnectorNotificationService sent " + count + " notifications for invalidation of " + result);
  }

  /**
   * Returns a list of all users that are configured to receive
   * tagging notifications.
   */
  private Collection<User> getNotificationUsers(ConnectorContext context) {
    List<String> groups = context.getNotificationsUserGroups();
    List<User> users = new ArrayList<>();
    for (String group : groups) {
      Group userGroup = contentRepository.getConnection().getUserRepository().getGroupByName(group);
      if (userGroup != null) {
        Collection<User> memberUsers = userGroup.getMemberUsers();
        for (User memberUser : memberUsers) {
          if (!users.contains(memberUser)) {
            users.add(memberUser);
          }
        }
      }
    }

    List<String> userList = context.getNotificationsUsers();
    for (String user : userList) {
      User userByName = contentRepository.getConnection().getUserRepository().getUserByName(user);
      if (userByName != null && !users.contains(userByName)) {
        users.add(userByName);
      }
    }

    return users;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }
}
