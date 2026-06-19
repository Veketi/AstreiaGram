package com.astreiagram.post_service.controller;

import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.dto.CommentRequest;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> create(HttpServletRequest httpRequest,
                                        @Valid @RequestBody CreatePostRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
        Post post = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getById(@PathVariable String id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Post> like(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(postService.likePost(id, userId));
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Post> unlike(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(postService.unlikePost(id, userId));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Post> comment(HttpServletRequest httpRequest,
                                         @PathVariable String id,
                                         @Valid @RequestBody CommentRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(postService.addComment(id, userId, request.content()));
    }
}
