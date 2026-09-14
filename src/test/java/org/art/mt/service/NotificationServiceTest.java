package org.art.mt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.art.mt.dto.NotificationDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.entity.Notification;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.repository.NotificationRepository;
import org.art.mt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    private NotificationService notificationService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);

        alice = new User("alice", "alice@example.com", "hashed");
        alice.setId(1L);
        bob = new User("bob", "bob@example.com", "hashed");
        bob.setId(2L);
    }

    private Notification existingNotification(User recipient, User actor) {
        Notification notification = new Notification();
        notification.setId(5L);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(Notification.Type.LIKE);
        notification.setPostId(10L);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    @Test
    void getNotifications_returnsPagedNotificationsForTheRecipient() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        Notification notification = existingNotification(alice, bob);
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> page = new PageImpl<>(List.of(notification), pageRequest, 1);
        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(alice, pageRequest))
                .thenReturn(page);

        PagedResponse<NotificationDTO> result = notificationService.getNotifications("alice", 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActor().getUsername()).isEqualTo("bob");
        assertThat(result.getContent().get(0).getType()).isEqualTo(Notification.Type.LIKE);
    }

    @Test
    void getUnreadCount_delegatesToTheRepository() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(notificationRepository.countByRecipientAndReadFalse(alice)).thenReturn(3L);

        assertThat(notificationService.getUnreadCount("alice")).isEqualTo(3L);
    }

    @Test
    void markRead_succeedsForTheRecipient() {
        Notification notification = existingNotification(alice, bob);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        notificationService.markRead(5L, "alice");

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markRead_rejectsWhenCallerIsNotTheRecipient() {
        Notification notification = existingNotification(alice, bob);
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markRead(5L, "bob"))
                .isInstanceOf(ForbiddenActionException.class);
        verify(notificationRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void markRead_rejectsWhenNotificationDoesNotExist() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(999L, "alice"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
