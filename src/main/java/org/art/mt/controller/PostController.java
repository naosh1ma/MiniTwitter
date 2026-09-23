package org.art.mt.controller;

import java.util.List;
import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.CommentDTO;
import org.art.mt.dto.CreateCommentDTO;
import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.LikeStatusDTO;
import org.art.mt.dto.PostDTO;
import org.art.mt.dto.PostFeedDTO;
import org.art.mt.service.CommentService;
import org.art.mt.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostDTO dto,
                                                           @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(postService.createPost(username, dto), "Post created"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id,
                                                        @AuthenticationPrincipal String username) {
        postService.deletePost(id, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Post deleted"));
    }

    @GetMapping("/feed")
    public ResponseEntity<PostFeedDTO> getPostFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var data = postService.getPostFeed(page, size);
        return ResponseEntity.ok(new PostFeedDTO(data.getContent()));
    }

    @GetMapping("/feed/following")
    public ResponseEntity<PostFeedDTO> getFollowingFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal String username) {
        var data = postService.getFollowingFeed(username, page, size);
        return ResponseEntity.ok(new PostFeedDTO(data.getContent()));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<LikeStatusDTO>> toggleLike(@PathVariable Long id,
                                                                 @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(postService.toggleLike(id, username), "Like toggled"));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ApiResponse<PostDTO>> attachImage(@PathVariable Long id,
                                                            @RequestParam("file") MultipartFile file,
                                                            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(postService.attachImage(id, username, file), "Image attached"));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(@PathVariable Long id,
                                                                 @Valid @RequestBody CreateCommentDTO dto,
                                                                 @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.createComment(id, username, dto), "Comment added"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(id), "Comments fetched"));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id,
                                                           @PathVariable Long commentId,
                                                           @AuthenticationPrincipal String username) {
        commentService.deleteComment(id, commentId, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Comment deleted"));
    }
}
