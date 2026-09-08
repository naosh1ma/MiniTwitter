import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from './components/header/header';
import { ApiService } from './services/api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule,
    HeaderComponent
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit {
  isAuthenticated = false;
  user: any = null;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.checkAuthStatus();
  }

  checkAuthStatus() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        this.user = JSON.parse(userStr);
        this.isAuthenticated = true;
      } catch (e) {
        console.error('Invalid user data in localStorage');
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }
  }

  onLoginSuccess() {
    this.checkAuthStatus();
  }
}