package com.astreiagram.post_service.repository;

import com.astreiagram.post_service.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserId(String userId);
}
