package org.art.mt.consumer;

import org.art.mt.config.KafkaTopics;
import org.art.mt.dto.NotificationEvent;
import org.art.mt.entity.Notification;
import org.art.mt.entity.User;
import org.art.mt.repository.NotificationRepository;
import org.art.mt.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * At-least-once delivery means the same event can arrive twice; the eventId
 * unique constraint on the notifications table is the idempotency guard, and
 * a violation here is treated as a harmless no-op - the exact same idiom
 * PostService.toggleLike already uses for concurrent double-likes.
 */
@Component
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationConsumer(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = KafkaTopics.NOTIFICATIONS, groupId = "minitwitter-notification-consumer")
    public void onNotificationEvent(NotificationEvent event) {
        try {
            User recipient = userRepository.findByUsername(event.getRecipientUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown recipient: " + event.getRecipientUsername()));
            User actor = userRepository.findByUsername(event.getActorUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown actor: " + event.getActorUsername()));

            Notification notification = new Notification();
            notification.setEventId(event.getEventId());
            notification.setType(event.getType());
            notification.setRecipient(recipient);
            notification.setActor(actor);
            notification.setPostId(event.getPostId());
            notificationRepository.save(notification);
        } catch (DataIntegrityViolationException e) {
            logger.debug("Duplicate notification event {} - already recorded, skipping", event.getEventId());
        } catch (IllegalArgumentException e) {
            logger.warn("Skipping notification event {}: {}", event.getEventId(), e.getMessage());
        }
    }
}
