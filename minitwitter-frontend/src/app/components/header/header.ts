import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class HeaderComponent implements OnInit {
  isAuthenticated = false;
  user: any = null;

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit() {
    this.checkAuthStatus();
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.checkAuthStatus();
      }
    });
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

  logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    this.isAuthenticated = false;
    this.user = null;
    // Redirect to auth page
    window.location.href = '/auth';
  }
}