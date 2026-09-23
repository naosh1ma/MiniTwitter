package org.art.mt.service;

import org.art.mt.dto.NotificationDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.entity.Notification;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.mapper.UserMapper;
import org.art.mt.repository.NotificationRepository;
import org.art.mt.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PagedResponse<NotificationDTO> getNotifications(String username, int page, int size) {
        User user = requireUser(username);
        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(
                user, PageRequest.of(page, size, NEWEST_FIRST));
        return PagedResponse.from(notifications,
                notifications.getContent().stream().map(this::toDTO).toList());
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientAndReadFalse(requireUser(username));
    }

    @Transactional
    public void markRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new ForbiddenActionException("You can only mark your own notifications as read");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(notification.getId(), notification.getType(),
                UserMapper.toDTO(notification.getActor()),
                notification.getPostId(), notification.isRead(), notification.getCreatedAt());
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
