import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';
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

  constructor(private apiService: ApiService) { }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.apiService.getProfile().subscribe({
      next: (response) => {
        if (response.success) {
          this.user = response.data;
        }
      },
      error: (error) => {
        console.error('Error loading profile:', error);
      }
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
    if (this.isEditing && this.user) {
      this.bio = this.user.bio || '';
    }
  }

  updateProfile() {
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

  onFileSelected(event: any) {
    this.avatarFile = event.target.files[0];
  }

  uploadAvatar() {
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