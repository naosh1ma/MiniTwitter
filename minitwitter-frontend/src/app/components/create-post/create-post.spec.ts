import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { CreatePostComponent } from './create-post';

describe('CreatePost', () => {
  let component: CreatePostComponent;
  let fixture: ComponentFixture<CreatePostComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreatePostComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreatePostComponent);
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

  it('onSubmit does nothing for blank content', () => {
    component.postContent = '   ';
    component.onSubmit();
    httpMock.expectNone(() => true);
    expect(component.isSubmitting).toBeFalse();
  });

  it('onSubmit creates the post and emits postCreated, without attaching an image, when none was picked', () => {
    spyOn(component.postCreated, 'emit');
    component.postContent = 'hello world';

    component.onSubmit();

    const req = httpMock.expectOne(r => r.url.endsWith('/posts'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'hello world' });
    req.flush({ success: true, data: { id: 1 }, message: '', timestamp: '' });

    httpMock.expectNone(r => r.url.includes('/image'));
    expect(component.postContent).toBe('');
    expect(component.postCreated.emit).toHaveBeenCalled();
  });

  it('onSubmit attaches the picked image after the post is created', () => {
    spyOn(component.postCreated, 'emit');
    component.postContent = 'with a photo';
    component.imageFile = new File(['fake'], 'photo.png', { type: 'image/png' });

    component.onSubmit();

    const createReq = httpMock.expectOne(r => r.url.endsWith('/posts'));
    createReq.flush({ success: true, data: { id: 5 }, message: '', timestamp: '' });

    const imageReq = httpMock.expectOne(r => r.url.endsWith('/posts/5/image'));
    expect(imageReq.request.body instanceof FormData).toBeTrue();
    imageReq.flush({ success: true, data: { id: 5 }, message: '', timestamp: '' });

    expect(component.imageFile).toBeNull();
    expect(component.postCreated.emit).toHaveBeenCalled();
  });

  it('removeImage clears the selected file and preview', () => {
    component.imageFile = new File(['fake'], 'photo.png', { type: 'image/png' });
    component.imagePreviewUrl = 'blob:fake-url';

    component.removeImage();

    expect(component.imageFile).toBeNull();
    expect(component.imagePreviewUrl).toBeNull();
  });
});
