import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { Post } from '../models/post';
import { Comment } from '../models/comment';
import { AppNotification } from '../models/notification';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { RegisterResponse } from '../models/register-response';
import { ApiResponse } from '../models/api-response';

@Injectable({
  providedIn: 'root'
})

export class ApiService {
  // Relative on purpose: in dev, proxy.conf.json forwards these to localhost:8080;
  // in production, Caddy reverse-proxies them to the backend on the same origin
  // the frontend is served from. Never hardcode a host here.
  private baseUrl = '/api';
  readonly apiOrigin = '';  // Used to resolve server-relative URLs like avatarUrl

  constructor(private http: HttpClient) { } // HTTP Client injizieren

  // Authentication
  //           Parameter                   Rückgabewert
  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    //                                       URL zu Spring Boot API       Parameter
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, loginRequest);
  }

  register(user: any): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/users/register`, user);
  }

  // Posts
  getPosts(page = 0, size = 20) {
    return this.http.get<{ posts: Post[] }>(`${this.baseUrl}/posts/feed?page=${page}&size=${size}`);
  }

  createPost(post: any): Observable<ApiResponse<Post>> {
    return this.http.post<ApiResponse<Post>>(`${this.baseUrl}/posts`, post);
  }

  deletePost(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/posts/${id}`);
  }

  toggleLike(postId: number): Observable<ApiResponse<{ liked: boolean; likeCount: number }>> {
    return this.http.post<ApiResponse<{ liked: boolean; likeCount: number }>>(`${this.baseUrl}/posts/${postId}/like`, {});
  }

  attachPostImage(postId: number, file: File): Observable<ApiResponse<Post>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<Post>>(`${this.baseUrl}/posts/${postId}/image`, formData);
  }

  getComments(postId: number): Observable<ApiResponse<Comment[]>> {
    return this.http.get<ApiResponse<Comment[]>>(`${this.baseUrl}/posts/${postId}/comments`);
  }

  addComment(postId: number, content: string): Observable<ApiResponse<Comment>> {
    return this.http.post<ApiResponse<Comment>>(`${this.baseUrl}/posts/${postId}/comments`, { content });
  }

  deleteComment(postId: number, commentId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/posts/${postId}/comments/${commentId}`);
  }

  getFollowingFeed(page = 0, size = 20) {
    return this.http.get<{ posts: Post[] }>(`${this.baseUrl}/posts/feed/following?page=${page}&size=${size}`);
  }

  followUser(username: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/users/${username}/follow`, {});
  }

  unfollowUser(username: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/users/${username}/follow`);
  }

  // Profile Management
  getProfile(): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.baseUrl}/users/profile`);
  }

  updateProfile(profileData: { bio: string; avatarUrl?: string }): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.baseUrl}/users/profile`, profileData);
  }

  uploadAvatar(file: File): Observable<ApiResponse<User>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ApiResponse<User>>(`${this.baseUrl}/users/profile/avatar`, formData);
  }

  getUserByUsername(username: string): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.baseUrl}/users/${username}`);
  }

  // Notifications
  getNotifications(page = 0, size = 20): Observable<ApiResponse<{ content: AppNotification[] }>> {
    return this.http.get<ApiResponse<{ content: AppNotification[] }>>(`${this.baseUrl}/notifications?page=${page}&size=${size}`);
  }

  getUnreadNotificationCount(): Observable<ApiResponse<{ count: number }>> {
    return this.http.get<ApiResponse<{ count: number }>>(`${this.baseUrl}/notifications/unread-count`);
  }

  markNotificationRead(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/notifications/${id}/read`, {});
  }

}