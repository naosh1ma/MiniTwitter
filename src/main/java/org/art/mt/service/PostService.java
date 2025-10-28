package org.art.mt.service;

import java.util.List;
import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.PagedResponse;
import org.art.mt.dto.PostDTO;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.repository.PostRepository;
import org.art.mt.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;


@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
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
        UserDTO authorDTO = convertToDTO(post.getAuthor());
        return new PostDTO(post.getId(), post.getContent(), authorDTO, post.getCreatedAt(), post.getUpdatedAt());
    }

    @Transactional
    public PagedResponse<PostDTO> getPostFeed(int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAll(pr);
        List<PostDTO> content = posts.getContent().stream().map(this::convertPostToDTO).toList();
        PagedResponse<PostDTO> resp = new PagedResponse<>();
        resp.setContent(content);
        resp.setPage(posts.getNumber());
        resp.setSize(posts.getSize());
        resp.setTotalElements(posts.getTotalElements());
        resp.setTotalPages(posts.getTotalPages());
        resp.setLast(posts.isLast());
        return resp;
      }

    private PostDTO convertPostToDTO(Post post) {
        UserDTO authorDTO = convertToDTO(post.getAuthor());
        return new PostDTO(
                post.getId(),
                post.getContent(),
                authorDTO,
                post.getCreatedAt(),
                post.getUpdatedAt());
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
