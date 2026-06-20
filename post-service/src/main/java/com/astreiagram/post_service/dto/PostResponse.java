package com.astreiagram.post_service.dto;

import java.time.LocalDateTime;

import com.astreiagram.post_service.model.Post;

public record PostResponse(
	String id,
	String userId,
	String imageUrl,
	String caption,
	LocalDateTime createdAt,
	int likeCount,
	int commentCount
) {
	public static PostResponse from(Post post) {
		return new PostResponse(
			post.id(), 
			post.userId(), 
			post.imageUrl(), 
			post.caption(), 
			post.date(), 
			post.likedByUserIds().size(), 
			post.comments().size()
		);
	}
}
