package org.art.mt.service;

import java.util.UUID;

import org.art.mt.config.KafkaTopics;
import org.art.mt.dto.NotificationEvent;
import org.art.mt.entity.Notification;
import org.art.mt.event.PostLikedEvent;
import org.art.mt.event.UserFollowedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges internal, in-process Spring events to the Kafka topic. AFTER_COMMIT
 * guarantees a notification is never published for a like/follow whose
 * transaction ultimately rolled back, and Spring falls back to firing
 * immediately when there is no active transaction (e.g. in unit tests).
 * KafkaTemplate.send is already non-blocking and won't fail the caller's
 * request even if the broker is unreachable; the whenComplete callback below
 * exists purely so a broken producer shows up in logs instead of vanishing.
 */
@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationEventListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostLiked(PostLikedEvent event) {
        publish(new NotificationEvent(UUID.randomUUID(), Notification.Type.LIKE,
                event.recipientUsername(), event.actorUsername(), event.postId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserFollowed(UserFollowedEvent event) {
        publish(new NotificationEvent(UUID.randomUUID(), Notification.Type.FOLLOW,
                event.recipientUsername(), event.actorUsername(), null));
    }

    private void publish(NotificationEvent event) {
        kafkaTemplate.send(KafkaTopics.NOTIFICATIONS, event.getRecipientUsername(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.warn("Failed to publish notification event {} to Kafka", event.getEventId(), ex);
                    }
                });
    }
}
