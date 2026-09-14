import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

import { ProfileComponent } from './profile';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let httpMock: HttpTestingController;
  let paramMap$: BehaviorSubject<ReturnType<typeof convertToParamMap>>;

  function setUpWithRouteParam(username: string | null) {
    paramMap$ = new BehaviorSubject(convertToParamMap(username ? { username } : {}));
    return TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { paramMap: paramMap$.asObservable() } },
      ],
    }).compileComponents();
  }

  afterEach(() => {
    httpMock.verify();
  });

  describe('viewing your own profile (no username route param)', () => {
    beforeEach(async () => {
      localStorage.clear();
      await setUpWithRouteParam(null);
      fixture = TestBed.createComponent(ProfileComponent);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
    });

    it('should create', () => {
      httpMock.expectOne(r => r.url.endsWith('/users/profile')).flush({ success: true, data: null, message: '', timestamp: '' });
      expect(component).toBeTruthy();
    });

    it('calls getProfile (not getUserByUsername) and marks isOwnProfile true', () => {
      const req = httpMock.expectOne(r => r.url.endsWith('/users/profile'));
      req.flush({ success: true, data: { username: 'alice', bio: 'hi' }, message: '', timestamp: '' });

      expect(component.isOwnProfile).toBeTrue();
      expect(component.user?.username).toBe('alice');
    });

    it('sets an error flag instead of hanging forever when the load fails', () => {
      const req = httpMock.expectOne(r => r.url.endsWith('/users/profile'));
      req.flush('boom', { status: 500, statusText: 'Server Error' });

      expect(component.error).toBeTrue();
      expect(component.user).toBeNull();
    });

    it('toggleEdit seeds the bio field from the loaded user', () => {
      const req = httpMock.expectOne(r => r.url.endsWith('/users/profile'));
      req.flush({ success: true, data: { username: 'alice', bio: 'existing bio' }, message: '', timestamp: '' });

      component.toggleEdit();

      expect(component.isEditing).toBeTrue();
      expect(component.bio).toBe('existing bio');
    });
  });

  describe('viewing someone else\'s profile', () => {
    beforeEach(async () => {
      localStorage.clear();
      localStorage.setItem('user', JSON.stringify({ username: 'bob' }));
      await setUpWithRouteParam('alice');
      fixture = TestBed.createComponent(ProfileComponent);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
    });

    it('calls getUserByUsername (not getProfile) and marks isOwnProfile false', () => {
      const req = httpMock.expectOne(r => r.url.endsWith('/users/alice'));
      req.flush({
        success: true,
        data: { username: 'alice', followedByCurrentUser: false, followerCount: 3 },
        message: '',
        timestamp: '',
      });

      expect(component.isOwnProfile).toBeFalse();
      expect(component.user?.username).toBe('alice');
    });

    it('toggleFollow follows when not already followed, and updates the count optimistically-confirmed', () => {
      const loadReq = httpMock.expectOne(r => r.url.endsWith('/users/alice'));
      loadReq.flush({
        success: true,
        data: { username: 'alice', followedByCurrentUser: false, followerCount: 3 },
        message: '',
        timestamp: '',
      });

      component.toggleFollow();

      const followReq = httpMock.expectOne(r => r.url.endsWith('/users/alice/follow'));
      expect(followReq.request.method).toBe('POST');
      followReq.flush({ success: true, data: null, message: '', timestamp: '' });

      expect(component.user?.followedByCurrentUser).toBeTrue();
      expect(component.user?.followerCount).toBe(4);
      expect(component.followBusy).toBeFalse();
    });

    it('toggleFollow unfollows when already followed', () => {
      const loadReq = httpMock.expectOne(r => r.url.endsWith('/users/alice'));
      loadReq.flush({
        success: true,
        data: { username: 'alice', followedByCurrentUser: true, followerCount: 3 },
        message: '',
        timestamp: '',
      });

      component.toggleFollow();

      const followReq = httpMock.expectOne(r => r.url.endsWith('/users/alice/follow'));
      expect(followReq.request.method).toBe('DELETE');
      followReq.flush({ success: true, data: null, message: '', timestamp: '' });

      expect(component.user?.followedByCurrentUser).toBeFalse();
      expect(component.user?.followerCount).toBe(2);
    });

    it('toggleFollow is a no-op while a previous toggle is still in flight', () => {
      const loadReq = httpMock.expectOne(r => r.url.endsWith('/users/alice'));
      loadReq.flush({
        success: true,
        data: { username: 'alice', followedByCurrentUser: false, followerCount: 3 },
        message: '',
        timestamp: '',
      });

      component.toggleFollow();
      component.toggleFollow(); // should be ignored - followBusy is already true

      // Only one request should have been made; httpMock.verify() in afterEach
      // would fail this test if a second, unconsumed request was also queued.
      const req = httpMock.expectOne(r => r.url.endsWith('/users/alice/follow'));
      req.flush({ success: true, data: null, message: '', timestamp: '' });
      expect(component.user?.followedByCurrentUser).toBeTrue();
    });
  });
});
