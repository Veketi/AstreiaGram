package com.astreiagram.userservice.service;

import com.astreiagram.userservice.dto.UserDtos.*;
import com.astreiagram.userservice.entity.Follow;
import com.astreiagram.userservice.entity.User;
import com.astreiagram.userservice.repository.FollowRepository;
import com.astreiagram.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        return toProfileResponse(findUserOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public void assertUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Usuário não encontrado: " + userId);
        }
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);
        if (request.getBio() != null)
            user.setBio(request.getBio());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public FollowListResponse getFollowers(UUID userId) {
        findUserOrThrow(userId);
        List<UUID> ids = followRepository.findFollowerIdsByFollowedId(userId);
        List<UserSummary> summaries = userRepository.findAllById(ids).stream()
                .map(this::toSummary)
                .toList();
        return FollowListResponse.builder().users(summaries).total(summaries.size()).build();
    }

    @Transactional(readOnly = true)
    public FollowListResponse getFollowing(UUID userId) {
        findUserOrThrow(userId);
        List<UUID> ids = followRepository.findFollowedIdsByFollowerId(userId);
        List<UserSummary> summaries = userRepository.findAllById(ids).stream()
                .map(this::toSummary)
                .toList();
        return FollowListResponse.builder().users(summaries).total(summaries.size()).build();
    }

    @Transactional
    public void follow(UUID followerId, UUID followedId) {
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("Não é possível seguir a si mesmo");
        }
        findUserOrThrow(followedId);
        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            return; // Idempotente — já segue
        }
        followRepository.save(Follow.builder()
                .followerId(followerId)
                .followedId(followedId)
                .build());
        log.info("Usuário {} passou a seguir {}", followerId, followedId);
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followedId) {
        findUserOrThrow(followedId);
        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
        log.info("Usuário {} deixou de seguir {}", followerId, followedId);
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + userId));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .followerCount(followRepository.countByFollowedId(user.getId()))
                .followingCount(followRepository.countByFollowerId(user.getId()))
                .build();
    }

    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
