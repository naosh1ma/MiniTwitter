import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AppComponent } from './app';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('renders the header', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-header')).toBeTruthy();
  });

  it('is not authenticated when localStorage has no user', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isAuthenticated).toBeFalse();
  });

  it('picks up the logged-in user from localStorage on init', () => {
    localStorage.setItem('user', JSON.stringify({ username: 'alice' }));
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isAuthenticated).toBeTrue();
    expect(fixture.componentInstance.user.username).toBe('alice');
  });

  it('clears storage and stays logged out when localStorage has malformed user JSON', () => {
    localStorage.setItem('user', 'not-valid-json');
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isAuthenticated).toBeFalse();
    expect(localStorage.getItem('user')).toBeNull();
  });
});
