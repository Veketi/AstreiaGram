package com.astreiagram.post_service.dto;

import java.time.LocalDateTime;

public record PostResponse(
	String id,
	String userId,
	String imageUrl,
	String caption,
	LocalDateTime createdAt,
	int likeCount,
	int commentCount
) {}
