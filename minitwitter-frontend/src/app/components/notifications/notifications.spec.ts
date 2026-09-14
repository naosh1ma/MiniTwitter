import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { NotificationsComponent } from './notifications';
import { AppNotification } from '../../models/notification';

function makeNotification(overrides: Partial<AppNotification> = {}): AppNotification {
  return {
    id: 1,
    type: 'LIKE',
    actor: { id: 2, username: 'bob', email: 'b@example.com', createdAt: '' },
    postId: 10,
    read: false,
    createdAt: '2026-01-01T00:00:00',
    ...overrides,
  };
}

describe('Notifications', () => {
  let component: NotificationsComponent;
  let fixture: ComponentFixture<NotificationsComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(NotificationsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads and displays notifications on init', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne(r => r.urlWithParams.includes('/notifications'));
    req.flush({ success: true, message: null, data: { content: [makeNotification()] } });

    expect(component.loading).toBeFalse();
    expect(component.notifications.length).toBe(1);
    expect(component.notifications[0].actor.username).toBe('bob');
  });

  it('shows an error state when the request fails', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne(r => r.urlWithParams.includes('/notifications'));
    req.flush('server error', { status: 500, statusText: 'Internal Server Error' });

    expect(component.loading).toBeFalse();
    expect(component.error).toBeTrue();
  });

  it('optimistically marks a notification as read and calls the API', () => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.urlWithParams.includes('/notifications'))
      .flush({ success: true, message: null, data: { content: [makeNotification()] } });

    component.markRead(component.notifications[0]);
    expect(component.notifications[0].read).toBeTrue();

    const readReq = httpMock.expectOne(r => r.url.endsWith('/notifications/1/read'));
    expect(readReq.request.method).toBe('POST');
    readReq.flush({});
  });

  it('rolls back the read state if marking as read fails', () => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.urlWithParams.includes('/notifications'))
      .flush({ success: true, message: null, data: { content: [makeNotification()] } });

    component.markRead(component.notifications[0]);
    const readReq = httpMock.expectOne(r => r.url.endsWith('/notifications/1/read'));
    readReq.flush('server error', { status: 500, statusText: 'Internal Server Error' });

    expect(component.notifications[0].read).toBeFalse();
  });

  it('describes LIKE and FOLLOW notifications with distinct labels', () => {
    expect(component.actionLabel(makeNotification({ type: 'LIKE' }))).toBe('liked your post');
    expect(component.actionLabel(makeNotification({ type: 'FOLLOW' }))).toBe('followed you');
  });
});
