import { Injectable } from '@angular/core';
import { User } from '../models/user';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

/**
 * The only place that knows how the session is stored in the browser. Every
 * component and the auth interceptor go through here, so the storage keys and
 * the shape of the persisted user exist in exactly one file.
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * The logged-in user, or null. Storage that can't be parsed is treated as no
   * session at all and cleared, so a corrupted entry can't wedge the app.
   */
  get user(): User | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      console.warn('Invalid user data in localStorage, clearing the session');
      this.logout();
      return null;
    }
  }

  get username(): string | null {
    return this.user?.username ?? null;
  }

  get isAuthenticated(): boolean {
    return this.user !== null;
  }

  login(token: string, user: User): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  logout(): void {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(TOKEN_KEY);
  }
}
