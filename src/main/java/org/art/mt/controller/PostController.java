package org.art.mt.controller;

import java.util.List;
import java.util.Map;
import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.CommentDTO;
import org.art.mt.dto.CreateCommentDTO;
import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.PostDTO;
import org.art.mt.dto.PostFeedDTO;
import org.art.mt.service.CommentService;
import org.art.mt.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(postService.createPost(dto), "Post created"));
      }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
              @RequestParam(defaultValue = "20") int size) {
          String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          var data = postService.getFollowingFeed(username, page, size);
          return ResponseEntity.ok(new PostFeedDTO(data.getContent()));
      }

      @PostMapping("/{id}/like")
      public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(@PathVariable Long id) {
          String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          Map<String, Object> result = postService.toggleLike(id, username);
          return ResponseEntity.ok(ApiResponse.ok(result, "Like toggled"));
      }

      @PostMapping("/{id}/image")
      public ResponseEntity<ApiResponse<PostDTO>> attachImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
          String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          PostDTO result = postService.attachImage(id, username, file);
          return ResponseEntity.ok(ApiResponse.ok(result, "Image attached"));
      }

      @PostMapping("/{id}/comments")
      public ResponseEntity<ApiResponse<CommentDTO>> createComment(@PathVariable Long id, @Valid @RequestBody CreateCommentDTO dto) {
          String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          CommentDTO result = commentService.createComment(id, username, dto);
          return ResponseEntity.ok(ApiResponse.ok(result, "Comment added"));
      }

      @GetMapping("/{id}/comments")
      public ResponseEntity<ApiResponse<List<CommentDTO>>> getComments(@PathVariable Long id) {
          return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(id), "Comments fetched"));
      }

      @DeleteMapping("/{id}/comments/{commentId}")
      public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
          String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          commentService.deleteComment(id, commentId, username);
          return ResponseEntity.ok(ApiResponse.ok(null, "Comment deleted"));
      }
}
