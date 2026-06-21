package com.astreiagram.post_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;

@Schema(description = "Dados necessários para a criação de uma nova postagem.")
public record CreatePostRequest(
	@Schema(description = "URL pública da imagem do post (já hospedada em algum storage/CDN).",
		example = "https://cdn.astreiagram.com/posts/8f3a-imagem.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
	@NonNull String imageUrl,

	@Schema(description = "Legenda/descrição da postagem.",
		example = "Pôr do sol em São Paulo hoje \u2600\uFE0F", requiredMode = Schema.RequiredMode.REQUIRED)
	@NonNull String caption
) {}
