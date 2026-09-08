package org.art.mt.service;

import java.util.List;
import java.util.Map;
import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.PostDTO;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Like;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.repository.FollowRepository;
import org.art.mt.repository.LikeRepository;
import org.art.mt.repository.PostRepository;
import org.art.mt.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;


@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final SecurityUtil securityUtil;
    private final FileStorageService fileStorageService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                        LikeRepository likeRepository, FollowRepository followRepository,
                        SecurityUtil securityUtil, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.followRepository = followRepository;
        this.securityUtil = securityUtil;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public PostDTO createPost(CreatePostDTO dto) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = new Post();
        post.setContent(dto.getContent());
        post.setAuthor(author);
        postRepository.save(post);
        return convertPostToDTO(post, username);
    }

    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenActionException("You can only delete your own posts");
        }
        postRepository.delete(post);
    }

    @Transactional
    public PostDTO attachImage(Long postId, String username, MultipartFile file) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenActionException("You can only add an image to your own posts");
        }
        String imageUrl = fileStorageService.storeImage(file, "post-" + postId, post.getImageUrl());
        post.setImageUrl(imageUrl);
        postRepository.save(post);
        return convertPostToDTO(post, username);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
            } catch (DataIntegrityViolationException e) {
                // Already liked by a concurrent request - treat as liked.
                liked = true;
            }
        }
        long likeCount = likeRepository.countByPost(post);
        return Map.of("liked", liked, "likeCount", likeCount);
    }

    @Transactional
    public PagedResponse<PostDTO> getPostFeed(int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAll(pr);
        return toPagedResponse(posts);
    }

    @Transactional
    public PagedResponse<PostDTO> getFollowingFeed(String username, int page, int size) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<User> following = followRepository.findFollowingUsers(currentUser);
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByAuthorInOrderByCreatedAtDesc(following, pr);
        return toPagedResponse(posts);
    }

    private PagedResponse<PostDTO> toPagedResponse(Page<Post> posts) {
        String currentUsername = securityUtil.getCurrentUsernameOrNull();
        List<PostDTO> content = posts.getContent().stream()
                .map(post -> convertPostToDTO(post, currentUsername))
                .toList();
        PagedResponse<PostDTO> resp = new PagedResponse<>();
        resp.setContent(content);
        resp.setPage(posts.getNumber());
        resp.setSize(posts.getSize());
        resp.setTotalElements(posts.getTotalElements());
        resp.setTotalPages(posts.getTotalPages());
        resp.setLast(posts.isLast());
        return resp;
    }

    private PostDTO convertPostToDTO(Post post, String currentUsernameOrNull) {
        UserDTO authorDTO = convertToDTO(post.getAuthor());
        PostDTO dto = new PostDTO(
                post.getId(),
                post.getContent(),
                authorDTO,
                post.getCreatedAt(),
                post.getUpdatedAt());
        dto.setImageUrl(post.getImageUrl());
        dto.setLikeCount(likeRepository.countByPost(post));
        if (currentUsernameOrNull != null) {
            userRepository.findByUsername(currentUsernameOrNull).ifPresent(user ->
                    dto.setLikedByCurrentUser(likeRepository.existsByPostAndUser(post, user)));
        }
        return dto;
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }
}
