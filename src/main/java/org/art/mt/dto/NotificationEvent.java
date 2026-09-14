package org.art.mt.dto;

import java.util.UUID;

import org.art.mt.entity.Notification;

/**
 * Kafka wire format for a notification-worthy event. Carries usernames rather
 * than entity references so the consumer can re-resolve them independently -
 * this is a message on a topic, not a JPA object.
 */
public class NotificationEvent {

    private UUID eventId;
    private Notification.Type type;
    private String recipientUsername;
    private String actorUsername;
    private Long postId;

    public NotificationEvent() {}

    public NotificationEvent(UUID eventId, Notification.Type type, String recipientUsername,
                              String actorUsername, Long postId) {
        this.eventId = eventId;
        this.type = type;
        this.recipientUsername = recipientUsername;
        this.actorUsername = actorUsername;
        this.postId = postId;
    }

    public UUID getEventId() {return eventId;}
    public void setEventId(UUID eventId) {this.eventId = eventId;}
    public Notification.Type getType() {return type;}
    public void setType(Notification.Type type) {this.type = type;}
    public String getRecipientUsername() {return recipientUsername;}
    public void setRecipientUsername(String recipientUsername) {this.recipientUsername = recipientUsername;}
    public String getActorUsername() {return actorUsername;}
    public void setActorUsername(String actorUsername) {this.actorUsername = actorUsername;}
    public Long getPostId() {return postId;}
    public void setPostId(Long postId) {this.postId = postId;}
}
