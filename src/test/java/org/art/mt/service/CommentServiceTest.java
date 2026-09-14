package org.art.mt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.art.mt.dto.CommentDTO;
import org.art.mt.dto.CreateCommentDTO;
import org.art.mt.entity.Comment;
import org.art.mt.entity.Post;
import org.art.mt.entity.User;
import org.art.mt.exception.ForbiddenActionException;
import org.art.mt.repository.CommentRepository;
import org.art.mt.repository.PostRepository;
import org.art.mt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;

    private CommentService commentService;

    private User alice;
    private User bob;
    private Post post;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, postRepository, userRepository);

        alice = new User("alice", "alice@example.com", "hashed");
        alice.setId(1L);
        bob = new User("bob", "bob@example.com", "hashed");
        bob.setId(2L);

        post = new Post();
        post.setId(10L);
        post.setContent("hello world");
        post.setAuthor(alice);
        post.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createComment_savesCommentAuthoredByCaller() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

        CommentDTO result = commentService.createComment(10L, "bob", new CreateCommentDTO("nice post!"));

        assertThat(result.getContent()).isEqualTo("nice post!");
        assertThat(result.getAuthor().getUsername()).isEqualTo("bob");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_rejectsWhenPostDoesNotExist() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(999L, "bob", new CreateCommentDTO("hi")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Comment existingComment(User author) {
        Comment comment = new Comment();
        comment.setId(5L);
        comment.setContent("nice post!");
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    @Test
    void deleteComment_succeedsWhenCallerIsTheAuthor() {
        Comment comment = existingComment(bob);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(10L, 5L, "bob");

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_rejectsWhenCallerIsNotTheAuthor() {
        Comment comment = existingComment(bob);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(10L, 5L, "alice"))
                .isInstanceOf(ForbiddenActionException.class);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteComment_rejectsWhenCommentBelongsToADifferentPost() {
        Comment comment = existingComment(bob);
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        // Comment belongs to post 10, but caller references post 999.
        assertThatThrownBy(() -> commentService.deleteComment(999L, 5L, "bob"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void getComments_returnsCommentsInChronologicalOrder() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        Comment comment = existingComment(bob);
        when(commentRepository.findByPostOrderByCreatedAtAsc(post)).thenReturn(List.of(comment));

        List<CommentDTO> result = commentService.getComments(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("nice post!");
    }
}
