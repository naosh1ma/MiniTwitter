package org.art.mt.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum Type { LIKE, FOLLOW }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User actor;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "event_id", unique = true, nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification() {}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public User getRecipient() {return recipient;}
    public void setRecipient(User recipient) {this.recipient = recipient;}
    public Type getType() {return type;}
    public void setType(Type type) {this.type = type;}
    public User getActor() {return actor;}
    public void setActor(User actor) {this.actor = actor;}
    public Long getPostId() {return postId;}
    public void setPostId(Long postId) {this.postId = postId;}
    public UUID getEventId() {return eventId;}
    public void setEventId(UUID eventId) {this.eventId = eventId;}
    public boolean isRead() {return read;}
    public void setRead(boolean read) {this.read = read;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
