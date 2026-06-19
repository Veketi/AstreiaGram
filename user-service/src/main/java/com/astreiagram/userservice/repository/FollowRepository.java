package com.astreiagram.userservice.repository;

import com.astreiagram.userservice.entity.Follow;
import com.astreiagram.userservice.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    @Query("SELECT f.followedId FROM Follow f WHERE f.followerId = :userId")
    List<UUID> findFollowedIdsByFollowerId(@Param("userId") UUID userId);

    @Query("SELECT f.followerId FROM Follow f WHERE f.followedId = :userId")
    List<UUID> findFollowerIdsByFollowedId(@Param("userId") UUID userId);

    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    long countByFollowedId(UUID followedId);

    long countByFollowerId(UUID followerId);
}
