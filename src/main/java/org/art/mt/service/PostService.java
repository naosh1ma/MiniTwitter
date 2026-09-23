package org.art.mt.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.LikeStatusDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.PostDTO;
import org.art.mt.entity.Like;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.mapper.UserMapper;
import org.art.mt.repository.CommentRepository;
import org.art.mt.repository.FollowRepository;
import org.art.mt.repository.LikeRepository;
import org.art.mt.repository.PostRepository;
import org.art.mt.repository.UserRepository;
import org.art.mt.event.PostLikedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;


@Service
public class PostService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final SecurityUtil securityUtil;
    private final FileStorageService fileStorageService;
    private final FeedCacheService feedCacheService;
    private final ApplicationEventPublisher eventPublisher;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                        LikeRepository likeRepository, FollowRepository followRepository,
                        CommentRepository commentRepository, SecurityUtil securityUtil,
                        FileStorageService fileStorageService, FeedCacheService feedCacheService,
                        ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.followRepository = followRepository;
        this.commentRepository = commentRepository;
        this.securityUtil = securityUtil;
        this.fileStorageService = fileStorageService;
        this.feedCacheService = feedCacheService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PostDTO createPost(String username, CreatePostDTO dto) {
        User author = requireUser(username);
        Post post = new Post();
        post.setContent(dto.getContent());
        post.setAuthor(author);
        postRepository.save(post);
        feedCacheService.evictPageZero();
        return toPostDTO(post, username);
    }

    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = requirePost(postId);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenActionException("You can only delete your own posts");
        }
        postRepository.delete(post);
        feedCacheService.evictPageZero();
    }

    @Transactional
    public PostDTO attachImage(Long postId, String username, MultipartFile file) {
        Post post = requirePost(postId);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenActionException("You can only add an image to your own posts");
        }
        String imageUrl = fileStorageService.storeImage(file, "post-" + postId, post.getImageUrl());
        post.setImageUrl(imageUrl);
        postRepository.save(post);
        return toPostDTO(post, username);
    }

    @Transactional
    public LikeStatusDTO toggleLike(Long postId, String username) {
        Post post = requirePost(postId);
        User user = requireUser(username);

        boolean liked;
        if (likeRepository.existsByPostAndUser(post, user)) {
            likeRepository.deleteByPostAndUser(post, user);
            liked = false;
        } else {
            try {
                Like like = new Like();
                like.setPost(post);
                like.setUser(user);
                likeRepository.save(like);
                liked = true;
                if (!post.getAuthor().getUsername().equals(username)) {
                    eventPublisher.publishEvent(new PostLikedEvent(
                            post.getAuthor().getUsername(), username, post.getId()));
                }
            } catch (DataIntegrityViolationException e) {
                // Already liked by a concurrent request - treat as liked.
                liked = true;
            }
        }
        return new LikeStatusDTO(liked, likeRepository.countByPost(post));
    }

    @Transactional
    public PagedResponse<PostDTO> getPostFeed(int page, int size) {
        String currentUsername = securityUtil.getCurrentUsernameOrNull();

        if (feedCacheService.isCacheable(page, size)) {
            PagedResponse<PostDTO> cached = feedCacheService.getPageZero();
            if (cached != null) {
                return overlayLikes(cached, currentUsername);
            }
            Page<Post> posts = postRepository.findAll(newestFirst(page, size));
            // Build with no current user, so the cached JSON never contains anyone's
            // personal like state - only impersonal data (likeCount, commentCount, etc).
            PagedResponse<PostDTO> base = toPagedResponse(posts, null);
            feedCacheService.putPageZero(base);
            return overlayLikes(base, currentUsername);
        }

        Page<Post> posts = postRepository.findAll(newestFirst(page, size));
        return toPagedResponse(posts, currentUsername);
    }

    @Transactional
    public PagedResponse<PostDTO> getFollowingFeed(String username, int page, int size) {
        List<User> following = followRepository.findFollowingUsers(requireUser(username));
        Page<Post> posts = postRepository.findByAuthorInOrderByCreatedAtDesc(following, newestFirst(page, size));
        return toPagedResponse(posts, username);
    }

    private PagedResponse<PostDTO> toPagedResponse(Page<Post> posts, String currentUsernameOrNull) {
        List<PostDTO> content = posts.getContent().stream()
                .map(post -> toPostDTO(post, currentUsernameOrNull))
                .toList();
        return PagedResponse.from(posts, content);
    }

    /**
     * Applies the current viewer's likedByCurrentUser state on top of an already-built
     * (and possibly cached, therefore impersonal) response, using one batched query
     * instead of one existsByPostAndUser call per post.
     */
    private PagedResponse<PostDTO> overlayLikes(PagedResponse<PostDTO> response, String currentUsernameOrNull) {
        if (currentUsernameOrNull == null || response.getContent().isEmpty()) {
            return response;
        }
        Optional<User> userOpt = userRepository.findByUsername(currentUsernameOrNull);
        if (userOpt.isEmpty()) {
            return response;
        }
        User user = userOpt.get();
        List<Long> postIds = response.getContent().stream().map(PostDTO::getId).toList();
        Set<Long> likedPostIds = new HashSet<>(likeRepository.findLikedPostIds(user, postIds));
        response.getContent().forEach(dto -> dto.setLikedByCurrentUser(likedPostIds.contains(dto.getId())));
        return response;
    }

    private PostDTO toPostDTO(Post post, String currentUsernameOrNull) {
        PostDTO dto = new PostDTO(
                post.getId(),
                post.getContent(),
                UserMapper.toDTO(post.getAuthor()),
                post.getCreatedAt(),
                post.getUpdatedAt());
        dto.setImageUrl(post.getImageUrl());
        dto.setLikeCount(likeRepository.countByPost(post));
        dto.setCommentCount(commentRepository.countByPost(post));
        if (currentUsernameOrNull != null) {
            userRepository.findByUsername(currentUsernameOrNull).ifPresent(user ->
                    dto.setLikedByCurrentUser(likeRepository.existsByPostAndUser(post, user)));
        }
        return dto;
    }

    private static Pageable newestFirst(int page, int size) {
        return PageRequest.of(page, size, NEWEST_FIRST);
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
