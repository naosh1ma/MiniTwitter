import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api';
import { AppNotification } from '../../models/notification';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications.html',
})
export class NotificationsComponent implements OnInit {
  notifications: AppNotification[] = [];
  loading = true;
  error = false;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getNotifications().subscribe({
      next: (res) => {
        this.notifications = res.data?.content ?? [];
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  markRead(notification: AppNotification) {
    if (notification.read) return;
    notification.read = true; // optimistic
    this.apiService.markNotificationRead(notification.id).subscribe({
      error: () => notification.read = false // roll back on failure
    });
  }

  actionLabel(notification: AppNotification): string {
    return notification.type === 'LIKE' ? 'liked your post' : 'followed you';
  }
}
