import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class HeaderComponent {
  @Output() logoutEvent = new EventEmitter<void>();
  
  isLoggedIn = false;
  currentUser: any = null;

  constructor() {
    this.checkAuthStatus();
  }

  checkAuthStatus() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    if (token && user) {
      try {
        this.currentUser = JSON.parse(user);
        this.isLoggedIn = true;
      } catch (e) {
        console.warn('Invalid user data in localStorage');
        this.isLoggedIn = false;
        this.currentUser = null;
      }
    } else {
      this.isLoggedIn = false;
      this.currentUser = null;
    }
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.isLoggedIn = false;
    this.currentUser = null;
    this.logoutEvent.emit();
  }
}