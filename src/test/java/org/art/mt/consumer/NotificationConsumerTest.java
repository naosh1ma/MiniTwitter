package org.art.mt.consumer;

import java.util.Optional;
import java.util.UUID;

import org.art.mt.dto.NotificationEvent;
import org.art.mt.entity.Notification;
import org.art.mt.entity.User;
import org.art.mt.repository.NotificationRepository;
import org.art.mt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    private NotificationConsumer consumer;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        consumer = new NotificationConsumer(notificationRepository, userRepository);

        alice = new User("alice", "alice@example.com", "hashed");
        alice.setId(1L);
        bob = new User("bob", "bob@example.com", "hashed");
        bob.setId(2L);
    }

    private NotificationEvent likeEvent() {
        return new NotificationEvent(UUID.randomUUID(), Notification.Type.LIKE, "alice", "bob", 10L);
    }

    @Test
    void onNotificationEvent_savesANotificationForAValidEvent() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

        consumer.onNotificationEvent(likeEvent());

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void onNotificationEvent_isANoOpForARedeliveredDuplicateEvent() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new DataIntegrityViolationException("eventId already exists"));

        assertThatCode(() -> consumer.onNotificationEvent(likeEvent())).doesNotThrowAnyException();
    }

    @Test
    void onNotificationEvent_skipsAnEventForAnUnknownRecipient() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        assertThatCode(() -> consumer.onNotificationEvent(likeEvent())).doesNotThrowAnyException();
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void onNotificationEvent_skipsAnEventForAnUnknownActor() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThatCode(() -> consumer.onNotificationEvent(likeEvent())).doesNotThrowAnyException();
        verify(notificationRepository, never()).save(any());
    }
}
