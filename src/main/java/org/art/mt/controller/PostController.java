package org.art.mt.controller;

import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.CreatePostDTO;
import org.art.mt.dto.PostDTO;
import org.art.mt.dto.PostFeedDTO;
import org.art.mt.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(postService.createPost(dto), "Post created"));
      }

      @GetMapping("/feed")
      public ResponseEntity<PostFeedDTO> getPostFeed(
              @RequestParam(defaultValue = "0") int page,
              @RequestParam(defaultValue = "20") int size) {
          var data = postService.getPostFeed(page, size);
          return ResponseEntity.ok(new PostFeedDTO(data.getContent()));
      }
}
