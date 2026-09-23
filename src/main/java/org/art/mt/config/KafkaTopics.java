package org.art.mt.config;

/**
 * Topic names shared by the producer (NotificationEventListener) and the
 * consumer (NotificationConsumer). They must agree exactly or events are
 * published to a topic nobody reads, which fails silently on both sides.
 */
public final class KafkaTopics {

    public static final String NOTIFICATIONS = "minitwitter.notifications";

    private KafkaTopics() {
    }
}
