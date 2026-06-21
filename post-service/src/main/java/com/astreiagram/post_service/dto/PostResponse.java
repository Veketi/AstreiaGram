package com.astreiagram.post_service.dto;

import java.time.LocalDateTime;

import com.astreiagram.post_service.model.Post;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação pública de uma postagem, já com os contadores de curtidas e comentários agregados.")
public record PostResponse(
	@Schema(description = "Identificador único do post (ObjectId do MongoDB).", example = "664f1c2e8a1b2c3d4e5f6789")
	String id,

	@Schema(description = "Identificador do usuário autor do post (vem do User Service).", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
	String userId,

	@Schema(description = "URL da imagem da postagem.", example = "https://cdn.astreiagram.com/posts/8f3a-imagem.jpg")
	String imageUrl,

	@Schema(description = "Legenda da postagem.", example = "Pôr do sol em São Paulo hoje \u2600\uFE0F")
	String caption,

	@Schema(description = "Data e hora de criação da postagem.", example = "2026-06-21T14:30:00")
	LocalDateTime createdAt,

	@Schema(description = "Quantidade total de curtidas.", example = "42")
	int likeCount,

	@Schema(description = "Quantidade total de comentários.", example = "7")
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
