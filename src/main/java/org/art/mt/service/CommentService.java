package org.art.mt.service;

import java.util.List;

import org.art.mt.dto.CommentDTO;
import org.art.mt.dto.CreateCommentDTO;
import org.art.mt.dto.UserDTO;
import org.art.mt.entity.Comment;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.repository.CommentRepository;
import org.art.mt.repository.PostRepository;
import org.art.mt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                           UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentDTO createComment(Long postId, String username, CreateCommentDTO dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setPost(post);
        comment.setAuthor(author);
        commentRepository.save(comment);
        return convertToDTO(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment not found");
        }
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenActionException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    public List<CommentDTO> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return commentRepository.findByPostOrderByCreatedAtAsc(post).stream()
                .map(this::convertToDTO)
                .toList();
    }

    private CommentDTO convertToDTO(Comment comment) {
        User user = comment.getAuthor();
        UserDTO authorDTO = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
        return new CommentDTO(comment.getId(), comment.getContent(), authorDTO, comment.getCreatedAt());
    }
}
