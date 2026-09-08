import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class AuthComponent {
  @Output() loginSuccess = new EventEmitter<void>();
  
  isLogin = true;
  loginRequest = { username: '', password: '' };
  registerData = { username: '', email: '', password: '' };

  constructor(private apiService: ApiService, private router: Router) {}

  onLogin() {
    this.apiService.login(this.loginRequest).subscribe({
      next: (response) => {
        if (response.success) {
          localStorage.setItem('token', response.data.token);
          localStorage.setItem('user', JSON.stringify(response.data.user));
          this.loginSuccess.emit();
          this.router.navigate(['/feed']);
        }
      },
      error: (error) => {
        console.error('Login failed:', error);
      }
    });
  }

  toggleMode() {
    this.isLogin = !this.isLogin;
  }

  onRegister() {
    this.apiService.register(this.registerData).subscribe({
      next: (response) => {
        if (response.success) {
          this.isLogin = true;
          this.registerData = { username: '', email: '', password: '' };
        }
      },
      error: (error) => {
        console.error('Registration failed:', error);
      }
    });
  }
}