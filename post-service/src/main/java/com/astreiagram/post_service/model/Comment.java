package com.astreiagram.post_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.CreatedDate;

public record Comment(
	String id,
	@NonNull String userId,
	@NonNull String content,
	@CreatedDate LocalDateTime createdAt
) {
	public Comment {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
	}
}
