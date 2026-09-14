import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { FeedComponent } from './feed';
import { Post } from '../../models/post';

function makePost(overrides: Partial<Post> = {}): Post {
  return {
    id: 1,
    content: 'hello',
    author: { id: 1, username: 'alice', email: 'a@example.com', createdAt: '' },
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '',
    likeCount: 0,
    likedByCurrentUser: false,
    commentCount: 0,
    ...overrides,
  };
}

describe('Feed', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [] });
    expect(component).toBeTruthy();
  });

  it('loads the "For You" feed on init and appends the returned posts', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(req => req.url.includes('/posts/feed?'));
    expect(req.request.method).toBe('GET');
    req.flush({ posts: [makePost({ id: 1, content: 'first post' })] });

    expect(component.posts.length).toBe(1);
    expect(component.posts[0].content).toBe('first post');
  });

  it('surfaces a distinct error state instead of silently showing an empty feed', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(req => req.url.includes('/posts/feed?'));
    req.flush('boom', { status: 500, statusText: 'Server Error' });

    expect(component.error).toBeTrue();
    expect(component.posts.length).toBe(0);
  });

  it('switchTab clears the current posts and calls the following-feed endpoint', () => {
    fixture.detectChanges();
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [makePost()] });
    expect(component.posts.length).toBe(1);

    component.switchTab('following');

    expect(component.posts.length).toBe(0);
    const req = httpMock.expectOne(req => req.url.includes('/posts/feed/following'));
    req.flush({ posts: [makePost({ id: 2, content: 'from someone I follow' })] });
    expect(component.posts[0].content).toBe('from someone I follow');
  });

  it('switchTab is a no-op when switching to the already-active tab', () => {
    fixture.detectChanges();
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [] });

    component.switchTab('forYou');

    httpMock.expectNone(req => req.url.includes('/posts/feed?'));
    expect(component.tab).toBe('forYou');
  });

  it('deletePost removes the post from the local list on success', () => {
    fixture.detectChanges();
    const post = makePost({ id: 42 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });
    expect(component.posts.length).toBe(1);

    component.deletePost(post);

    const req = httpMock.expectOne(req => req.url.endsWith('/posts/42'));
    expect(req.request.method).toBe('DELETE');
    req.flush({ data: null, message: 'Post deleted', success: true, timestamp: '' });

    expect(component.posts.length).toBe(0);
  });

  it('deletePost leaves the post in place when the request fails', () => {
    fixture.detectChanges();
    const post = makePost({ id: 42 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });

    component.deletePost(post);

    const req = httpMock.expectOne(req => req.url.endsWith('/posts/42'));
    req.flush('forbidden', { status: 403, statusText: 'Forbidden' });

    expect(component.posts.length).toBe(1);
  });

  it('toggleLike applies an optimistic update immediately, then reconciles with the server response', () => {
    fixture.detectChanges();
    const post = makePost({ id: 7, likeCount: 3, likedByCurrentUser: false });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });

    component.toggleLike(post);

    // Optimistic update happens synchronously, before the HTTP response arrives.
    expect(post.likedByCurrentUser).toBeTrue();
    expect(post.likeCount).toBe(4);

    const req = httpMock.expectOne(req => req.url.endsWith('/posts/7/like'));
    expect(req.request.method).toBe('POST');
    req.flush({ data: { liked: true, likeCount: 4 }, message: '', success: true, timestamp: '' });

    expect(post.likedByCurrentUser).toBeTrue();
    expect(post.likeCount).toBe(4);
  });

  it('toggleLike rolls back the optimistic update if the request fails', () => {
    fixture.detectChanges();
    const post = makePost({ id: 7, likeCount: 3, likedByCurrentUser: false });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });

    component.toggleLike(post);
    expect(post.likeCount).toBe(4);

    const req = httpMock.expectOne(req => req.url.endsWith('/posts/7/like'));
    req.flush('error', { status: 500, statusText: 'Server Error' });

    expect(post.likedByCurrentUser).toBeFalse();
    expect(post.likeCount).toBe(3);
  });

  it('currentUsername reads the username out of localStorage', () => {
    fixture.detectChanges();
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [] });

    expect(component.currentUsername).toBeNull();

    localStorage.setItem('user', JSON.stringify({ username: 'bob' }));
    expect(component.currentUsername).toBe('bob');
  });

  it('toggleComments expands a post and lazy-loads its comments on first open', () => {
    fixture.detectChanges();
    const post = makePost({ id: 3 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });

    component.toggleComments(post);

    expect(component.expandedPostId).toBe(3);
    const req = httpMock.expectOne(req => req.url.endsWith('/posts/3/comments'));
    expect(req.request.method).toBe('GET');
    req.flush({ data: [{ id: 1, content: 'nice!', author: { id: 2, username: 'bob' }, createdAt: '' }], message: '', success: true, timestamp: '' });

    expect(component.commentsByPostId[3].length).toBe(1);
  });

  it('toggleComments collapses an already-expanded post without a new request', () => {
    fixture.detectChanges();
    const post = makePost({ id: 3 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });

    component.toggleComments(post);
    httpMock.expectOne(req => req.url.endsWith('/posts/3/comments')).flush({ data: [], message: '', success: true, timestamp: '' });

    component.toggleComments(post);

    expect(component.expandedPostId).toBeNull();
  });

  it('toggleComments does not refetch on a second expand once comments are already loaded', () => {
    fixture.detectChanges();
    const post = makePost({ id: 3 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });

    component.toggleComments(post); // expand + load
    httpMock.expectOne(req => req.url.endsWith('/posts/3/comments')).flush({ data: [], message: '', success: true, timestamp: '' });
    component.toggleComments(post); // collapse

    component.toggleComments(post); // expand again

    httpMock.expectNone(req => req.url.endsWith('/posts/3/comments'));
    expect(component.expandedPostId).toBe(3);
  });

  it('addComment posts the comment and appends it locally, bumping commentCount', () => {
    fixture.detectChanges();
    const post = makePost({ id: 3, commentCount: 0 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });
    component.newCommentText[3] = 'great post';

    component.addComment(post);

    const req = httpMock.expectOne(req => req.url.endsWith('/posts/3/comments'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'great post' });
    req.flush({ data: { id: 5, content: 'great post', author: { id: 1, username: 'alice' }, createdAt: '' }, message: '', success: true, timestamp: '' });

    expect(component.commentsByPostId[3].length).toBe(1);
    expect(post.commentCount).toBe(1);
    expect(component.newCommentText[3]).toBe('');
  });

  it('addComment does nothing for blank text', () => {
    fixture.detectChanges();
    const post = makePost({ id: 3, commentCount: 0 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });
    component.newCommentText[3] = '   ';

    component.addComment(post);
    expect(post.commentCount).toBe(0);

    httpMock.expectNone(req => req.url.endsWith('/posts/3/comments'));
  });

  it('deleteComment removes the comment locally and decrements commentCount', () => {
    fixture.detectChanges();
    const post = makePost({ id: 3, commentCount: 1 });
    httpMock.expectOne(req => req.url.includes('/posts/feed?')).flush({ posts: [post] });
    component.commentsByPostId[3] = [{ id: 9, content: 'x', author: { id: 2, username: 'bob' } as any, createdAt: '' }];

    component.deleteComment(post, component.commentsByPostId[3][0]);

    const req = httpMock.expectOne(req => req.url.endsWith('/posts/3/comments/9'));
    expect(req.request.method).toBe('DELETE');
    req.flush({ data: null, message: '', success: true, timestamp: '' });

    expect(component.commentsByPostId[3].length).toBe(0);
    expect(post.commentCount).toBe(0);
  });
});
