package com.astreiagram.post_service.controller;

import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.dto.PostResponse;
import com.astreiagram.post_service.dto.CommentRequest;
import com.astreiagram.post_service.model.Comment;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<PostResponse> create(
			HttpServletRequest httpRequest,
            @Valid @RequestBody CreatePostRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
        Post post = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(PostResponse.from(postService.getPostById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getByUserId(
			@PathVariable String userId,
			@PageableDefault(size = 50, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<PostResponse> posts = postService.getPostsByUserId(userId, pageable)
			.map(PostResponse::from);
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Void> like(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
		postService.likePost(id, userId);
        return ResponseEntity.noContent().build();
    }

	@GetMapping("/{id}/likes")
	public ResponseEntity<List<String>> getLikes(
			@PathVariable String id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		return ResponseEntity.ok(postService.getLikes(id, page, size));
	}

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlike(HttpServletRequest httpRequest, @PathVariable String id) {
        String userId = (String) httpRequest.getAttribute("userId");
		postService.unlikePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> comment(HttpServletRequest httpRequest,
                                         @PathVariable String id,
                                         @Valid @RequestBody CommentRequest request) {
        String userId = (String) httpRequest.getAttribute("userId");
		postService.addComment(id, userId, request.content());
        return ResponseEntity.noContent().build();
    }

	@GetMapping("/{id}/comments")
	public ResponseEntity<List<Comment>> getComments(
			@PathVariable String id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(postService.getComments(id, page, size));
	}

	@DeleteMapping("{id}/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(HttpServletRequest httpRequest,
			@PathVariable String id,
			@PathVariable String commentId) {
		String userId = (String) httpRequest.getAttribute("userId");
		postService.deleteComment(id, commentId, userId);
		return ResponseEntity.noContent().build();
	}
}
