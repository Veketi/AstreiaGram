package com.astreiagram.post_service.service;

import com.astreiagram.post_service.client.UserClient;
import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.event.PostCreatedEvent;
import com.astreiagram.post_service.event.PostEventPublisher;
import com.astreiagram.post_service.exception.CommentNotFoundException;
import com.astreiagram.post_service.exception.PostNotFoundException;
import com.astreiagram.post_service.exception.UnauthorizedException;
import com.astreiagram.post_service.exception.UserNotFoundException;
import com.astreiagram.post_service.model.Comment;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.repository.PostRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
	private final UserClient userClient;
	private final PostEventPublisher eventPublisher;

    public PostService(PostRepository postRepository, UserClient userClient, PostEventPublisher eventPublisher) {
        this.postRepository = postRepository;
		this.userClient = userClient;
		this.eventPublisher = eventPublisher;
    }

    public Post createPost(String userId, CreatePostRequest request) {
        Post post = new Post(
            null,
            userId,
            request.imageUrl(),
            request.caption(),
            LocalDateTime.now(),
            new ArrayList<>(),
            new ArrayList<>()
        );

		Post postSaved = postRepository.save(post);

		eventPublisher.publishPostCreated(new PostCreatedEvent(
			postSaved.id(), 
			postSaved.userId(),
			postSaved.date().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
		));

        return postSaved;
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new PostNotFoundException(id));
    }

	public List<Post> getPostsByIds(List<String> ids) {
		return postRepository.findByIdIn(ids);
	}

    public Page<Post> getPostsByUserId(String userId, Pageable pageable) {
		if(!userClient.userExists(userId)) {
			throw new UserNotFoundException(userId);
		}
        return postRepository.findByUserId(userId, pageable);
    }

    public void deletePost(String id, String userId) {
		Post post = getPostById(id);

        if (!post.userId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this post.");
        }
        postRepository.deleteById(id);
    }

    public Post likePost(String postId, String userId) {
        Post post = getPostById(postId);

        if (!post.likedByUserIds().contains(userId)) {
            post.likedByUserIds().add(userId);
            return postRepository.save(post);
        }
        return post;
    }

	public List<String> getLikes(String postId, int page, int size) {
		Post post = getPostById(postId);
		List<String> all = post.likedByUserIds();

		int start = page * size;
		if (start >= all.size()) return List.of();

		int end = Math.min(start + size, all.size());
		return all.subList(start, end);
	}

    public Post unlikePost(String postId, String userId) {
        Post post = getPostById(postId);
		System.out.println("Antes: " + post.likedByUserIds());
        boolean removed = post.likedByUserIds().remove(userId);
		System.out.println("Removido? " + removed + " | Depois: " + post.likedByUserIds());
        return postRepository.save(post);
    }

    public Post addComment(String postId, String userId, String content) {
        Post post = getPostById(postId);

        Comment comment = new Comment(null, userId, content, LocalDateTime.now());
        post.comments().add(comment);

        return postRepository.save(post);
    }

	public List<Comment> getComments(String postId, int page, int size) {
		Post post = getPostById(postId);
		List<Comment> all = post.comments();

		int start = page * size;
		if (start >= all.size()) return List.of();

		int end = Math.min(start + size, all.size());
		return all.subList(start, end);
	}

	public void deleteComment(String postId, String commentId, String userId) {
		Post post = getPostById(postId);

		Comment comment = post.comments().stream()
			.filter(c -> c.id().equals(commentId))
			.findFirst()
			.orElseThrow(() -> new CommentNotFoundException(postId));

		if(!comment.userId().equals(userId)) {
			throw new UnauthorizedException("You don't have permission to delete this comment.");
		}

		post.comments().remove(comment);
		postRepository.save(post);
	}
}
