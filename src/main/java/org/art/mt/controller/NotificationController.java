package org.art.mt.controller;

import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.NotificationDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.UnreadCountDTO;
import org.art.mt.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDTO>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotifications(username, page, size), null));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountDTO>> getUnreadCount(@AuthenticationPrincipal String username) {
        UnreadCountDTO count = new UnreadCountDTO(notificationService.getUnreadCount(username));
        return ResponseEntity.ok(ApiResponse.ok(count, null));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id,
                                                      @AuthenticationPrincipal String username) {
        notificationService.markRead(id, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Notification marked as read"));
    }
}
