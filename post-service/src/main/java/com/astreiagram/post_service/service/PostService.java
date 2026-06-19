package com.astreiagram.post_service.service;

import com.astreiagram.post_service.client.UserClient;
import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.event.PostCreatedEvent;
import com.astreiagram.post_service.event.PostEventPublisher;
import com.astreiagram.post_service.exception.PostNotFoundException;
import com.astreiagram.post_service.model.Comment;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
	private final PostEventPublisher eventPublisher;

    public PostService(PostRepository postRepository, PostEventPublisher eventPublisher) {
        this.postRepository = postRepository;
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

    public List<Post> getPostsByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    public void deletePost(String id, String userId) {
		Post post = getPostById(id);

        if (!post.userId().equals(userId)) {
            throw new PostNotFoundException(id);
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

    public Post unlikePost(String postId, String userId) {
        Post post = getPostById(postId);
        post.likedByUserIds().remove(userId);
        return postRepository.save(post);
    }

    public Post addComment(String postId, String userId, String content) {
        Post post = getPostById(postId);

        Comment comment = new Comment(null, userId, content, LocalDateTime.now());
        post.comments().add(comment);

        return postRepository.save(post);
    }
}
