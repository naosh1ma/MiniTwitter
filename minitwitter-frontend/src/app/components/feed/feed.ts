import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CreatePostComponent } from '../create-post/create-post';
import { ApiService } from '../../services/api';
import { Post } from '../../models/post';
import { Comment } from '../../models/comment';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, CreatePostComponent],
  templateUrl: './feed.html',
  styleUrls: ['./feed.css']
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  loading = false;
  error = false;
  tab: 'forYou' | 'following' = 'forYou';

  expandedPostId: number | null = null;
  commentsByPostId: Record<number, Comment[]> = {};
  commentsLoading: Record<number, boolean> = {};
  newCommentText: Record<number, string> = {};

  constructor(private apiService: ApiService) { }

  ngOnInit() {
    this.loadPosts();
  }

  get apiOrigin(): string {
    return this.apiService.apiOrigin;
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

  toggleComments(post: Post) {
    if (this.expandedPostId === post.id) {
      this.expandedPostId = null;
      return;
    }
    this.expandedPostId = post.id;
    if (!this.commentsByPostId[post.id]) {
      this.commentsLoading[post.id] = true;
      this.apiService.getComments(post.id).subscribe({
        next: (res) => {
          this.commentsByPostId[post.id] = res.data;
          this.commentsLoading[post.id] = false;
        },
        error: (error) => {
          console.error('Error loading comments:', error);
          this.commentsLoading[post.id] = false;
        }
      });
    }
  }

  addComment(post: Post) {
    const content = (this.newCommentText[post.id] || '').trim();
    if (!content) return;

    this.apiService.addComment(post.id, content).subscribe({
      next: (res) => {
        this.commentsByPostId[post.id] = [...(this.commentsByPostId[post.id] || []), res.data];
        this.newCommentText[post.id] = '';
        post.commentCount = (post.commentCount || 0) + 1;
      },
      error: (error) => {
        console.error('Error adding comment:', error);
      }
    });
  }

  deleteComment(post: Post, comment: Comment) {
    this.apiService.deleteComment(post.id, comment.id).subscribe({
      next: () => {
        this.commentsByPostId[post.id] = (this.commentsByPostId[post.id] || []).filter(c => c.id !== comment.id);
        post.commentCount = Math.max(0, (post.commentCount || 0) - 1);
      },
      error: (error) => {
        console.error('Error deleting comment:', error);
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
