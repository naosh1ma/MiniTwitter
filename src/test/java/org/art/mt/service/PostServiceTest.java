package org.art.mt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.LikeStatusDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.PostDTO;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.event.PostLikedEvent;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.repository.CommentRepository;
import org.art.mt.repository.FollowRepository;
import org.art.mt.repository.LikeRepository;
import org.art.mt.repository.PostRepository;
import org.art.mt.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private LikeRepository likeRepository;
    @Mock private FollowRepository followRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private FeedCacheService feedCacheService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private PostService postService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        // SecurityUtil is a real instance (not mocked) so it exercises the real
        // anonymous-vs-authenticated logic against whatever we put in the SecurityContext.
        postService = new PostService(postRepository, userRepository, likeRepository,
                followRepository, commentRepository, new SecurityUtil(), mock(FileStorageService.class),
                feedCacheService, eventPublisher);

        alice = new User("alice", "alice@example.com", "hashed");
        alice.setId(1L);
        bob = new User("bob", "bob@example.com", "hashed");
        bob.setId(2L);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    private Post existingPostBy(User author) {
        Post post = new Post();
        post.setId(10L);
        post.setContent("hello world");
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }

    @Test
    void createPost_savesPostAuthoredByTheGivenUser() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        PostDTO result = postService.createPost("alice", new CreatePostDTO("hello world"));

        assertThat(result.getContent()).isEqualTo("hello world");
        assertThat(result.getAuthor().getUsername()).isEqualTo("alice");
        verify(postRepository).save(any(Post.class));
        verify(feedCacheService).evictPageZero();
    }

    @Test
    void deletePost_succeedsWhenCallerIsTheAuthor() {
        Post post = existingPostBy(alice);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        postService.deletePost(10L, "alice");

        verify(postRepository).delete(post);
        verify(feedCacheService).evictPageZero();
    }

    @Test
    void deletePost_rejectsWhenCallerIsNotTheAuthor() {
        Post post = existingPostBy(alice);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(10L, "bob"))
                .isInstanceOf(ForbiddenActionException.class);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void deletePost_rejectsWhenPostDoesNotExist() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(999L, "alice"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toggleLike_likesWhenNotAlreadyLiked() {
        Post post = existingPostBy(alice);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(likeRepository.existsByPostAndUser(post, bob)).thenReturn(false);
        when(likeRepository.countByPost(post)).thenReturn(1L);

        LikeStatusDTO result = postService.toggleLike(10L, "bob");

        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(1L);
        verify(eventPublisher).publishEvent(new PostLikedEvent("alice", "bob", 10L));
    }

    @Test
    void toggleLike_unlikesWhenAlreadyLiked() {
        Post post = existingPostBy(alice);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(likeRepository.existsByPostAndUser(post, bob)).thenReturn(true);
        when(likeRepository.countByPost(post)).thenReturn(0L);

        LikeStatusDTO result = postService.toggleLike(10L, "bob");

        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isEqualTo(0L);
        verify(likeRepository).deleteByPostAndUser(post, bob);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void toggleLike_doesNotPublishAnEventForASelfLike() {
        Post post = existingPostBy(alice);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(likeRepository.existsByPostAndUser(post, alice)).thenReturn(false);
        when(likeRepository.countByPost(post)).thenReturn(1L);

        postService.toggleLike(10L, "alice");

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getPostFeed_anonymousUser_neverMarksPostsAsLiked() {
        // No authentication set up -> SecurityUtil sees no authenticated user.
        when(feedCacheService.isCacheable(0, 20)).thenReturn(true);
        Post post = existingPostBy(alice);
        Page<Post> page = new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);
        when(postRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(likeRepository.countByPost(post)).thenReturn(3L);

        PagedResponse<PostDTO> result = postService.getPostFeed(0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isLikedByCurrentUser()).isFalse();
        assertThat(result.getContent().get(0).getLikeCount()).isEqualTo(3L);
        // Anonymous callers should never trigger a "does this user like this post" lookup.
        verify(likeRepository, never()).existsByPostAndUser(any(), any());
    }

    @Test
    void getFollowingFeed_onlyReturnsPostsFromFollowedUsers() {
        authenticateAs("bob");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(followRepository.findFollowingUsers(bob)).thenReturn(List.of(alice));
        Post post = existingPostBy(alice);
        Page<Post> page = new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);
        when(postRepository.findByAuthorInOrderByCreatedAtDesc(eq(List.of(alice)), any(PageRequest.class)))
                .thenReturn(page);

        PagedResponse<PostDTO> result = postService.getFollowingFeed("bob", 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthor().getUsername()).isEqualTo("alice");
        verify(postRepository, times(1)).findByAuthorInOrderByCreatedAtDesc(eq(List.of(alice)), any(PageRequest.class));
    }

    @Test
    void getPostFeed_cacheMiss_buildsFromDbAndPopulatesTheCache() {
        when(feedCacheService.isCacheable(0, 20)).thenReturn(true);
        Post post = existingPostBy(alice);
        Page<Post> page = new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);
        when(feedCacheService.getPageZero()).thenReturn(null);
        when(postRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(likeRepository.countByPost(post)).thenReturn(2L);

        postService.getPostFeed(0, 20);

        verify(postRepository, times(1)).findAll(any(PageRequest.class));
        verify(feedCacheService).putPageZero(any());
    }

    @Test
    void getPostFeed_cacheHit_overlaysLikesWithoutHittingTheDatabaseAgain() {
        authenticateAs("bob");
        when(feedCacheService.isCacheable(0, 20)).thenReturn(true);
        PostDTO cachedPost = new PostDTO();
        cachedPost.setId(10L);
        cachedPost.setLikedByCurrentUser(false); // cached data is always impersonal
        PagedResponse<PostDTO> cached = new PagedResponse<>(List.of(cachedPost), 0, 20, 1, 1, true);
        when(feedCacheService.getPageZero()).thenReturn(cached);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(likeRepository.findLikedPostIds(bob, List.of(10L))).thenReturn(List.of(10L));

        PagedResponse<PostDTO> result = postService.getPostFeed(0, 20);

        assertThat(result.getContent().get(0).isLikedByCurrentUser()).isTrue();
        verify(postRepository, never()).findAll(any(PageRequest.class));
        verify(feedCacheService, never()).putPageZero(any());
    }

    @Test
    void getPostFeed_nonDefaultPageSize_bypassesTheCacheEntirely() {
        // isCacheable(1, 10) is false by default on the mock, mirroring the real service.
        Post post = existingPostBy(alice);
        Page<Post> page = new PageImpl<>(List.of(post), PageRequest.of(1, 10), 1);
        when(postRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(likeRepository.countByPost(post)).thenReturn(0L);

        postService.getPostFeed(1, 10);

        verify(feedCacheService, never()).getPageZero();
        verify(feedCacheService, never()).putPageZero(any());
    }
}
