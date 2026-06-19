package com.astreiagram.post_service.event;

public record PostCreatedEvent(
    String postId,
    String authorId,
    long createdAt
) {}
