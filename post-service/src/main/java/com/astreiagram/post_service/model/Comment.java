package com.astreiagram.post_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.CreatedDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record Comment(
	@Schema(description = "Identificador único do comentário (UUID gerado pelo serviço).", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
	String id,

	@Schema(description = "Identificador do usuário autor do comentário.", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
	@NonNull String userId,

	@Schema(description = "Texto do comentário.", example = "Que foto incrível!")
	@NonNull String content,

	@Schema(description = "Data e hora em que o comentário foi criado.", example = "2026-06-21T14:35:00")
	@CreatedDate LocalDateTime createdAt
) {
	public Comment {
		if (id == null) {
			id = UUID.randomUUID().toString();
		}
	}
}
