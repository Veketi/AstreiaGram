package com.astreiagram.post_service.exception;

public class CommentNotFoundException extends RuntimeException {
	public CommentNotFoundException(String id) {
		super("Comment not found: " + id);
	}
}
