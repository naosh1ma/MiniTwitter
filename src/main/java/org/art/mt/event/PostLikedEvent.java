package org.art.mt.event;

/**
 * Internal Spring application event, published from inside PostService.toggleLike's
 * existing transaction and consumed only after that transaction commits (see
 * NotificationEventListener) - never published for the unlike branch or for a
 * self-like, since neither should notify anyone.
 */
public record PostLikedEvent(String recipientUsername, String actorUsername, Long postId) {
}
