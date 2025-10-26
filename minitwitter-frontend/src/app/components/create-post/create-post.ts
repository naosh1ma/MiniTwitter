import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-post.html',
  styleUrls: ['./create-post.css']
})
export class CreatePostComponent {
  postContent = '';
  isSubmitting = false;

  constructor(private apiService: ApiService) {}

  onSubmit() {
    if (!this.postContent.trim()) return;

    this.isSubmitting = true;
    
    // Get current user from localStorage
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    
    const postData = {
      content: this.postContent,
      author: {
        id: user.id,
        username: user.username
      }
    };

    this.apiService.createPost(postData).subscribe({
      next: (response) => {
        console.log('Post created:', response);
        this.postContent = '';
        this.isSubmitting = false;
        // Optionally emit event to refresh feed
      },
      error: (error) => {
        console.error('Error creating post:', error);
        this.isSubmitting = false;
      }
    });
  }
}