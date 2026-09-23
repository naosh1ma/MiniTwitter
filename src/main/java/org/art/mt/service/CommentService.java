package org.art.mt.service;

import java.util.List;

import org.art.mt.dto.CommentDTO;
import org.art.mt.dto.CreateCommentDTO;
import org.art.mt.entity.Comment;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.mapper.UserMapper;
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
        Post post = requirePost(postId);
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setPost(post);
        comment.setAuthor(author);
        commentRepository.save(comment);
        return toDTO(comment);
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
        return commentRepository.findByPostOrderByCreatedAtAsc(requirePost(postId)).stream()
                .map(this::toDTO)
                .toList();
    }

    private CommentDTO toDTO(Comment comment) {
        return new CommentDTO(comment.getId(), comment.getContent(),
                UserMapper.toDTO(comment.getAuthor()), comment.getCreatedAt());
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }
}
