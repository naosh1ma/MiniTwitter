import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';

import { AuthComponent } from './auth';

describe('Auth', () => {
  let component: AuthComponent;
  let fixture: ComponentFixture<AuthComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AuthComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('toggleMode flips between login and register', () => {
    expect(component.isLogin).toBeTrue();
    component.toggleMode();
    expect(component.isLogin).toBeFalse();
  });

  it('onLogin stores the token/user and navigates to /feed on success', () => {
    spyOn(router, 'navigate');
    component.loginRequest = { username: 'alice', password: 'pw' };

    component.onLogin();

    const req = httpMock.expectOne(r => r.url.endsWith('/auth/login'));
    req.flush({
      success: true,
      data: { token: 'jwt-token', user: { username: 'alice' } },
      message: '',
      timestamp: '',
    });

    expect(localStorage.getItem('token')).toBe('jwt-token');
    expect(JSON.parse(localStorage.getItem('user')!).username).toBe('alice');
    expect(router.navigate).toHaveBeenCalledWith(['/feed']);
  });

  it('onLogin does not store anything when the server reports failure', () => {
    component.loginRequest = { username: 'alice', password: 'wrong' };

    component.onLogin();

    const req = httpMock.expectOne(r => r.url.endsWith('/auth/login'));
    req.flush({ success: false, data: null, message: 'Invalid password', timestamp: '' });

    expect(localStorage.getItem('token')).toBeNull();
  });

  it('onRegister switches back to login mode and clears the form on success', () => {
    component.isLogin = false;
    component.registerData = { username: 'bob', email: 'bob@example.com', password: 'pw123456' };

    component.onRegister();

    const req = httpMock.expectOne(r => r.url.endsWith('/users/register'));
    req.flush({ success: true, data: null, message: 'User registered successfully', timestamp: '' });

    expect(component.isLogin).toBeTrue();
    expect(component.registerData).toEqual({ username: '', email: '', password: '' });
  });
});
