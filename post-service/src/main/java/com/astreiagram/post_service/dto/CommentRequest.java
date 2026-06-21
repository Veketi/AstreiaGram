package com.astreiagram.post_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Conteúdo de um novo comentário a ser adicionado a um post.")
public record CommentRequest(
	@Schema(description = "Texto do comentário. Não pode ser vazio.",
		example = "Que foto incrível!", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank String content
) {}
