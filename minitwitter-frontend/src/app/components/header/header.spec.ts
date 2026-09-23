import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { HeaderComponent } from './header';

describe('Header', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows logged-out state when there is no user in localStorage', () => {
    expect(component.isAuthenticated).toBeFalse();
  });

  it('shows logged-in state and the username once a user is in localStorage', () => {
    localStorage.setItem('user', JSON.stringify({ username: 'alice' }));
    component.checkAuthStatus();
    expect(component.isAuthenticated).toBeTrue();
    expect(component.user?.username).toBe('alice');

    httpMock.expectOne('/api/notifications/unread-count')
      .flush({ success: true, message: null, data: { count: 0 } });
  });

  it('fetches and displays the unread notification count once authenticated', () => {
    localStorage.setItem('user', JSON.stringify({ username: 'alice' }));
    component.checkAuthStatus();

    httpMock.expectOne('/api/notifications/unread-count')
      .flush({ success: true, message: null, data: { count: 4 } });

    expect(component.unreadCount).toBe(4);
  });

  it('leaves the unread count untouched when the fetch fails', () => {
    localStorage.setItem('user', JSON.stringify({ username: 'alice' }));
    component.checkAuthStatus();

    httpMock.expectOne('/api/notifications/unread-count')
      .flush('server error', { status: 500, statusText: 'Internal Server Error' });

    expect(component.unreadCount).toBe(0);
  });

  // Note: logout() isn't covered here - it assigns window.location.href to
  // redirect to /auth, and a real headless browser won't let a test intercept
  // or safely trigger actual navigation without disrupting the test runner.
});
