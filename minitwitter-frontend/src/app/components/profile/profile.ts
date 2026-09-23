import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api';
import { AuthStore } from '../../services/auth-store';
import { User } from '../../models/user';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  isEditing = false;
  bio: string = '';
  avatarFile: File | null = null;
  isOwnProfile = true;
  followBusy = false;
  error = false;

  constructor(
    private apiService: ApiService,
    private authStore: AuthStore,
    private route: ActivatedRoute
  ) { }

  get currentUsername(): string | null {
    return this.authStore.username;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const username = params.get('username');
      this.user = null;
      this.error = false;
      if (username && username !== this.currentUsername) {
        this.isOwnProfile = false;
        this.loadUserByUsername(username);
      } else {
        this.isOwnProfile = true;
        this.loadProfile();
      }
    });
  }

  loadProfile(): void {
    this.apiService.getProfile().subscribe({
      next: (response) => {
        if (response.success) {
          this.user = response.data;
        }
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.error = true;
      }
    });
  }

  loadUserByUsername(username: string): void {
    this.apiService.getUserByUsername(username).subscribe({
      next: (response) => {
        if (response.success) {
          this.user = response.data;
        }
      },
      error: (error) => {
        console.error('Error loading user:', error);
        this.error = true;
      }
    });
  }

  toggleFollow(): void {
    if (!this.user || this.followBusy) return;
    this.followBusy = true;
    const wasFollowed = this.user.followedByCurrentUser;
    const action = wasFollowed
      ? this.apiService.unfollowUser(this.user.username)
      : this.apiService.followUser(this.user.username);

    action.subscribe({
      next: () => {
        if (this.user) {
          this.user.followedByCurrentUser = !wasFollowed;
          this.user.followerCount = (this.user.followerCount || 0) + (wasFollowed ? -1 : 1);
        }
        this.followBusy = false;
      },
      error: (error) => {
        console.error('Error toggling follow:', error);
        this.followBusy = false;
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (this.isEditing && this.user) {
      this.bio = this.user.bio || '';
    }
  }

  updateProfile(): void {
    if (!this.user) return;

    this.apiService.updateProfile({ bio: this.bio }).subscribe({
      next: (response) => {
        if (response.success) {
          this.user = response.data;
          this.isEditing = false;
        }
      },
      error: (error) => {
        console.error('Error updating profile:', error);
      }
    });
  }

  onFileSelected(event: Event): void {
    this.avatarFile = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  uploadAvatar(): void {
    if (!this.avatarFile || !this.user) return;

    this.apiService.uploadAvatar(this.avatarFile).subscribe({
      next: (response) => {
        if (response.success) {
          this.user = response.data;
          this.avatarFile = null;
        }
      },
      error: (error) => {
        console.error('Error uploading avatar:', error);
      }
    });
  }
}
