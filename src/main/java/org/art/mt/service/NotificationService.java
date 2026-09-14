package org.art.mt.service;

import org.art.mt.dto.NotificationDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Notification;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.repository.NotificationRepository;
import org.art.mt.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PagedResponse<NotificationDTO> getNotifications(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pr);

        PagedResponse<NotificationDTO> resp = new PagedResponse<>();
        resp.setContent(notifications.getContent().stream().map(this::toDTO).toList());
        resp.setPage(notifications.getNumber());
        resp.setSize(notifications.getSize());
        resp.setTotalElements(notifications.getTotalElements());
        resp.setTotalPages(notifications.getTotalPages());
        resp.setLast(notifications.isLast());
        return resp;
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.countByRecipientAndReadFalse(user);
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
        User actor = notification.getActor();
        UserDTO actorDTO = new UserDTO(actor.getId(), actor.getUsername(), actor.getEmail(),
                actor.getBio(), actor.getAvatarUrl(), actor.getCreatedAt());
        return new NotificationDTO(notification.getId(), notification.getType(), actorDTO,
                notification.getPostId(), notification.isRead(), notification.getCreatedAt());
    }
}
