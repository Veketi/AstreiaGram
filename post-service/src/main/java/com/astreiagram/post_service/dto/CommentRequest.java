package com.astreiagram.post_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
    @NotBlank String content
) {}
