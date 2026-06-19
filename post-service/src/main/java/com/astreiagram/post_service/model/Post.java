package com.astreiagram.post_service.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.Nullable;


@Document(collection = "posts")
public record Post(
	@Id String id,
	@Indexed
	@NonNull String userId,
	@NonNull String imageUrl,
	@Nullable String caption,
	@CreatedDate LocalDateTime date,
	List<String> likedByUserIds,
	List<Comment> comments
) {
	public Post {
		likedByUserIds = likedByUserIds != null ? likedByUserIds : new ArrayList<>();
		comments = comments != null ? comments : new ArrayList<>();
	}
}
