import { Component, EventEmitter, Output } from '@angular/core';
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
  @Output() postCreated = new EventEmitter<void>();
  postContent = '';
  isSubmitting = false;

  constructor(private apiService: ApiService) {}

  onSubmit() {
    if (!this.postContent.trim()) return;

    this.isSubmitting = true;
    
    const postData = { content: this.postContent };

    this.apiService.createPost(postData).subscribe({
      next: (response) => {
        console.log('Post created:', response);
        this.postContent = '';
        this.isSubmitting = false;
        this.postCreated.emit();
        
      },
      error: (error) => {
        console.error('Error creating post:', error);
        this.isSubmitting = false;
      }
    });
  }
}