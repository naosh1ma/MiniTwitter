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
  imageFile: File | null = null;
  imagePreviewUrl: string | null = null;

  constructor(private apiService: ApiService) {}

  onFileSelected(event: any) {
    const file = event.target.files?.[0] ?? null;
    this.imageFile = file;
    if (this.imagePreviewUrl) {
      URL.revokeObjectURL(this.imagePreviewUrl);
    }
    this.imagePreviewUrl = file ? URL.createObjectURL(file) : null;
  }

  removeImage() {
    this.imageFile = null;
    if (this.imagePreviewUrl) {
      URL.revokeObjectURL(this.imagePreviewUrl);
      this.imagePreviewUrl = null;
    }
  }

  onSubmit() {
    if (!this.postContent.trim()) return;

    this.isSubmitting = true;

    const postData = { content: this.postContent };

    this.apiService.createPost(postData).subscribe({
      next: (response) => {
        const postId = response.data.id;
        if (this.imageFile) {
          this.apiService.attachPostImage(postId, this.imageFile).subscribe({
            next: () => this.finishSubmit(),
            error: (error) => {
              console.error('Error attaching image:', error);
              this.finishSubmit();
            }
          });
        } else {
          this.finishSubmit();
        }
      },
      error: (error) => {
        console.error('Error creating post:', error);
        this.isSubmitting = false;
      }
    });
  }

  private finishSubmit() {
    this.postContent = '';
    this.removeImage();
    this.isSubmitting = false;
    this.postCreated.emit();
  }
}
