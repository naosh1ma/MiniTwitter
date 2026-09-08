import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CreatePostComponent } from '../create-post/create-post';
import { ApiService } from '../../services/api';
import { Post } from '../../models/post';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, CreatePostComponent],
  templateUrl: './feed.html',
  styleUrls: ['./feed.css']
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  loading = false;
  error = false;

  constructor(private apiService: ApiService) { }

  ngOnInit() {
    this.loadPosts();
  }

  page = 0; size = 20; last = false;

  loadPosts() {
    if (this.last) return;
    this.error = false;
    this.apiService.getPosts(this.page, this.size).subscribe({
      next: (res) => {
        // Backend sendet { posts: [...] } - nicht { data: { content: [...] } }
        this.posts = [...this.posts, ...res.posts];
        this.page = this.page + 1;
        this.last = res.posts.length < this.size; // Einfache Last-Check
      },
      error: (error) => {
        console.error('Error loading posts:', error);
        this.loading = false;
        this.error = true;
      }
    });
  }

  onPostCreated() {
    this.page = 0; this.last = false; this.posts = [];
    this.loadPosts();
  }

}