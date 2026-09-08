import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { Post } from '../models/post';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { RegisterResponse } from '../models/register-response';
import { ApiResponse } from '../models/api-response';

@Injectable({
  providedIn: 'root'
})

export class ApiService {
  private baseUrl = 'http://localhost:8080/api';  // Spring Boot API URL
  readonly apiOrigin = 'http://localhost:8080';  // Used to resolve server-relative URLs like avatarUrl

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



}