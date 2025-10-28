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

  createPost(post: any): Observable<Post> {
    return this.http.post<Post>(`${this.baseUrl}/posts`, post);
  }
}