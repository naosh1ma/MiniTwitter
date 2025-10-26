import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './components/header/header';
import { AuthComponent } from './components/auth/auth';
import { FeedComponent } from './components/feed/feed';
import { CreatePostComponent } from './components/create-post/create-post';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, HeaderComponent, AuthComponent, FeedComponent, CreatePostComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  isLoggedIn = false;

  constructor() {
    this.checkAuthStatus();
  }

  checkAuthStatus() {
    const token = localStorage.getItem('token');
    this.isLoggedIn = !!token;
  }

  onLoginSuccess() {
    this.isLoggedIn = true;
  }

  onLogout() {
    this.isLoggedIn = false;
  }
}