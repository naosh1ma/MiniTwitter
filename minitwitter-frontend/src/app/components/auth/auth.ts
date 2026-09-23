import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api';
import { AuthStore } from '../../services/auth-store';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class AuthComponent {
  isLogin = true;
  loginRequest = { username: '', password: '' };
  registerData = { username: '', email: '', password: '' };

  constructor(
    private apiService: ApiService,
    private authStore: AuthStore,
    private router: Router
  ) {}

  onLogin(): void {
    this.apiService.login(this.loginRequest).subscribe({
      next: (response) => {
        if (response.success) {
          this.authStore.login(response.data.token, response.data.user);
          this.router.navigate(['/feed']);
        }
      },
      error: (error) => {
        console.error('Login failed:', error);
      }
    });
  }

  toggleMode(): void {
    this.isLogin = !this.isLogin;
  }

  onRegister(): void {
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