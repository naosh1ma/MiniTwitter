import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CreatePostComponent } from '../create-post/create-post';
import { ApiService } from '../../services/api';
import { Post } from '../../models/post';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, RouterLink, CreatePostComponent],
  templateUrl: './feed.html',
  styleUrls: ['./feed.css']
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  loading = false;
  error = false;
  tab: 'forYou' | 'following' = 'forYou';

  constructor(private apiService: ApiService) { }

  ngOnInit() {
    this.loadPosts();
  }

  get currentUsername(): string | null {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr).username;
    } catch {
      return null;
    }
  }

  page = 0; size = 20; last = false;

  switchTab(tab: 'forYou' | 'following') {
    if (this.tab === tab) return;
    this.tab = tab;
    this.page = 0; this.last = false; this.posts = [];
    this.loadPosts();
  }

  loadPosts() {
    if (this.last) return;
    this.error = false;
    const request = this.tab === 'forYou'
      ? this.apiService.getPosts(this.page, this.size)
      : this.apiService.getFollowingFeed(this.page, this.size);
    request.subscribe({
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

  deletePost(post: Post) {
    this.apiService.deletePost(post.id).subscribe({
      next: () => {
        this.posts = this.posts.filter(p => p.id !== post.id);
      },
      error: (error) => {
        console.error('Error deleting post:', error);
      }
    });
  }

  toggleLike(post: Post) {
    const wasLiked = post.likedByCurrentUser;
    const previousCount = post.likeCount;
    // Optimistic update
    post.likedByCurrentUser = !wasLiked;
    post.likeCount = wasLiked ? previousCount - 1 : previousCount + 1;

    this.apiService.toggleLike(post.id).subscribe({
      next: (res) => {
        post.likedByCurrentUser = res.data.liked;
        post.likeCount = res.data.likeCount;
      },
      error: (error) => {
        console.error('Error toggling like:', error);
        post.likedByCurrentUser = wasLiked;
        post.likeCount = previousCount;
      }
    });
  }

}
