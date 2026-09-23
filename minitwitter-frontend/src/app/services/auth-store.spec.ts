import { TestBed } from '@angular/core/testing';
import { AuthStore } from './auth-store';

describe('AuthStore', () => {
  let store: AuthStore;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    store = TestBed.inject(AuthStore);
  });

  it('reports no session when localStorage is empty', () => {
    expect(store.isAuthenticated).toBeFalse();
    expect(store.user).toBeNull();
    expect(store.username).toBeNull();
    expect(store.token).toBeNull();
  });

  it('reads the persisted user', () => {
    localStorage.setItem('user', JSON.stringify({ username: 'alice' }));
    localStorage.setItem('token', 'jwt-token');

    expect(store.isAuthenticated).toBeTrue();
    expect(store.username).toBe('alice');
    expect(store.token).toBe('jwt-token');
  });

  it('clears the session when the stored user is malformed JSON', () => {
    localStorage.setItem('user', 'not-valid-json');
    localStorage.setItem('token', 'jwt-token');

    expect(store.user).toBeNull();
    expect(store.isAuthenticated).toBeFalse();
    expect(localStorage.getItem('user')).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('login persists both the token and the user', () => {
    store.login('jwt-token', { id: 1, username: 'alice', email: 'alice@example.com', createdAt: '2026-01-01' });

    expect(localStorage.getItem('token')).toBe('jwt-token');
    expect(store.username).toBe('alice');
  });

  it('logout removes both keys', () => {
    store.login('jwt-token', { id: 1, username: 'alice', email: 'alice@example.com', createdAt: '2026-01-01' });

    store.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(store.isAuthenticated).toBeFalse();
  });
});
