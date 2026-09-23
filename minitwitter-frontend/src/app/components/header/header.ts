import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';
import { AuthStore } from '../../services/auth-store';
import { User } from '../../models/user';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class HeaderComponent implements OnInit {
  isAuthenticated = false;
  user: User | null = null;
  unreadCount = 0;

  constructor(
    private apiService: ApiService,
    private authStore: AuthStore,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkAuthStatus();
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.checkAuthStatus();
      }
    });
  }

  checkAuthStatus(): void {
    this.user = this.authStore.user;
    this.isAuthenticated = this.user !== null;
    if (this.isAuthenticated) {
      this.refreshUnreadCount();
    }
  }

  refreshUnreadCount(): void {
    this.apiService.getUnreadNotificationCount().subscribe({
      next: (res) => this.unreadCount = res.data?.count ?? 0,
      error: () => {} // Notification badge is non-critical; a failed fetch just leaves the count stale.
    });
  }

  logout(): void {
    this.authStore.logout();
    this.isAuthenticated = false;
    this.user = null;
    // Redirect to auth page
    window.location.href = '/auth';
  }
}
