package org.art.mt.dto;

import java.time.LocalDateTime;

import org.art.mt.entity.Notification;

public class NotificationDTO {

    private Long id;
    private Notification.Type type;
    private UserDTO actor;
    private Long postId;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(Long id, Notification.Type type, UserDTO actor, Long postId,
                            boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.actor = actor;
        this.postId = postId;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Notification.Type getType() {return type;}
    public void setType(Notification.Type type) {this.type = type;}
    public UserDTO getActor() {return actor;}
    public void setActor(UserDTO actor) {this.actor = actor;}
    public Long getPostId() {return postId;}
    public void setPostId(Long postId) {this.postId = postId;}
    public boolean isRead() {return read;}
    public void setRead(boolean read) {this.read = read;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
