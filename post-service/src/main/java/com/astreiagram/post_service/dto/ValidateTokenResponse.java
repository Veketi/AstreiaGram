package com.astreiagram.post_service.dto;

public record ValidateTokenResponse(boolean valid, String userId, String username) {}
