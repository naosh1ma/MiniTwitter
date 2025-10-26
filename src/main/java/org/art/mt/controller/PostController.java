package org.art.mt.controller;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostDTO createPostDTO) {
        PostDTO postDTO = postService.createPost(createPostDTO);
        return ResponseEntity.ok(postDTO);
    }

    @GetMapping("/feed")
    public ResponseEntity<PostFeedDTO> getPostFeed() {
        PostFeedDTO postFeedDTO = postService.getPostFeed();
        return ResponseEntity.ok(postFeedDTO);
    }
}
