import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';
import { LoginRequest } from '../../models/login-request';
import { LoginResponse } from '../../models/login-response';

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
  loginRequest: LoginRequest = { username: '', password: '' };
  registerData = { username: '', email: '', password: '' };

  constructor(private apiService: ApiService) {}

  toggleMode() {
    this.isLogin = !this.isLogin;
  }

  onLogin() {
    this.apiService.login(this.loginRequest).subscribe({
      next: (response: LoginResponse) => {
        console.log('Login successful:', response);
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));
        this.loginSuccess.emit();
      },
      error: (error) => {
        console.error('Login failed:', error);
      }
    });
  }

  onRegister() {
    this.apiService.register(this.registerData).subscribe({
      next: (response: string) => {
        console.log('Registration successful:', response);
        this.isLogin = true;
      },
      error: (error) => {
        console.error('Registration failed:', error);
      }
    });
  }
}