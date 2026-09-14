import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { ApiService } from './api';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login posts credentials to /auth/login', () => {
    service.login({ username: 'alice', password: 'pw' }).subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/auth/login'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'alice', password: 'pw' });
    req.flush({});
  });

  it('getPosts requests the feed with page/size query params', () => {
    service.getPosts(2, 10).subscribe();
    const req = httpMock.expectOne(r => r.urlWithParams.includes('/posts/feed'));
    expect(req.request.urlWithParams).toContain('page=2');
    expect(req.request.urlWithParams).toContain('size=10');
    req.flush({ posts: [] });
  });

  it('getFollowingFeed hits the /posts/feed/following endpoint', () => {
    service.getFollowingFeed(0, 20).subscribe();
    const req = httpMock.expectOne(r => r.url.includes('/posts/feed/following'));
    expect(req.request.method).toBe('GET');
    req.flush({ posts: [] });
  });

  it('deletePost issues a DELETE to /posts/{id}', () => {
    service.deletePost(5).subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/posts/5'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('toggleLike POSTs to /posts/{id}/like', () => {
    service.toggleLike(5).subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/posts/5/like'));
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('followUser POSTs to /users/{username}/follow', () => {
    service.followUser('bob').subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/users/bob/follow'));
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('unfollowUser DELETEs /users/{username}/follow', () => {
    service.unfollowUser('bob').subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/users/bob/follow'));
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });

  it('uploadAvatar sends the file as multipart form data', () => {
    const file = new File(['fake'], 'avatar.png', { type: 'image/png' });
    service.uploadAvatar(file).subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/users/profile/avatar'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({});
  });

  it('attachPostImage sends the file as multipart form data to /posts/{id}/image', () => {
    const file = new File(['fake'], 'photo.png', { type: 'image/png' });
    service.attachPostImage(9, file).subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/posts/9/image'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({});
  });

  it('getNotifications requests the notifications list with page/size query params', () => {
    service.getNotifications(1, 10).subscribe();
    const req = httpMock.expectOne(r => r.urlWithParams.includes('/notifications'));
    expect(req.request.method).toBe('GET');
    expect(req.request.urlWithParams).toContain('page=1');
    expect(req.request.urlWithParams).toContain('size=10');
    req.flush({ success: true, message: null, data: { content: [] } });
  });

  it('getUnreadNotificationCount GETs /notifications/unread-count', () => {
    service.getUnreadNotificationCount().subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/notifications/unread-count'));
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, message: null, data: { count: 0 } });
  });

  it('markNotificationRead POSTs to /notifications/{id}/read', () => {
    service.markNotificationRead(7).subscribe();
    const req = httpMock.expectOne(r => r.url.endsWith('/notifications/7/read'));
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
