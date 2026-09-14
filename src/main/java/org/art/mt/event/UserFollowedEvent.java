package org.art.mt.event;

public record UserFollowedEvent(String recipientUsername, String actorUsername) {
}
