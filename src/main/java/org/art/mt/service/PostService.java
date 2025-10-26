package org.art.mt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.PostDTO;
import org.art.mt.dto.PostFeedDTO;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostDTO createPost(CreatePostDTO createPostDTO) {
        Post post = new Post();
        post.setContent(createPostDTO.getContent());
        post.setAuthor(createPostDTO.getAuthor());
        postRepository.save(post);
        UserDTO authorDTO = convertToDTO(post.getAuthor());
        return new PostDTO(post.getId(), post.getContent(), authorDTO, post.getCreatedAt(), post.getUpdatedAt());
    }

    public PostFeedDTO getPostFeed() {
        List<Post> posts = postRepository.findAll();
        List<PostDTO> postDTOs = posts.stream().map(this::convertPostToDTO).collect(Collectors.toList());
        return new PostFeedDTO(postDTOs);
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
