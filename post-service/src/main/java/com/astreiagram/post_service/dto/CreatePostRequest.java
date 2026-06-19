package com.astreiagram.post_service.dto;

import org.jspecify.annotations.NonNull;

public record CreatePostRequest(
	@NonNull String imageUrl,
	@NonNull String caption
) {}
