package com.astreiagram.userservice;

import com.astreiagram.userservice.dto.UserDtos.*;
import com.astreiagram.userservice.entity.Follow;
import com.astreiagram.userservice.entity.User;
import com.astreiagram.userservice.repository.FollowRepository;
import com.astreiagram.userservice.repository.UserRepository;
import com.astreiagram.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private FollowRepository followRepository;
    private UserService userService;

    private User buildUser(UUID id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .email(username + "@email.com")
                .password("hashed")
                .bio("Bio de " + username)
                .avatarUrl("http://avatar/" + username)
                .active(true)
                .build();
    }

    @BeforeEach
    void setUp() {
        userRepository  = mock(UserRepository.class);
        followRepository = mock(FollowRepository.class);
        userService     = new UserService(userRepository, followRepository);
    }

    // ── getUserProfile ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserProfile")
    class GetUserProfile {

        @Test
        @DisplayName("returns profile with follower and following counts")
        void shouldReturnProfileWithCounts() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "joao");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(followRepository.countByFollowedId(id)).thenReturn(10L);
            when(followRepository.countByFollowerId(id)).thenReturn(5L);

            UserProfileResponse profile = userService.getUserProfile(id);

            assertThat(profile.getId()).isEqualTo(id);
            assertThat(profile.getUsername()).isEqualTo("joao");
            assertThat(profile.getEmail()).isEqualTo("joao@email.com");
            assertThat(profile.getFollowerCount()).isEqualTo(10L);
            assertThat(profile.getFollowingCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("throws NoSuchElementException when user not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserProfile(id))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // ── assertUserExists ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("assertUserExists")
    class AssertUserExists {

        @Test
        @DisplayName("does nothing when user exists")
        void shouldNotThrowWhenExists() {
            UUID id = UUID.randomUUID();
            when(userRepository.existsById(id)).thenReturn(true);

            assertThatNoException().isThrownBy(() -> userService.assertUserExists(id));
        }

        @Test
        @DisplayName("throws NoSuchElementException when user does not exist")
        void shouldThrowWhenNotExists() {
            UUID id = UUID.randomUUID();
            when(userRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> userService.assertUserExists(id))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates bio and avatarUrl when both provided")
        void shouldUpdateBothFields() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "joao");
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);
            when(followRepository.countByFollowedId(id)).thenReturn(0L);
            when(followRepository.countByFollowerId(id)).thenReturn(0L);

            UpdateProfileRequest req = new UpdateProfileRequest("Nova bio", "http://new-avatar.jpg");
            userService.updateProfile(id, req);

            assertThat(user.getBio()).isEqualTo("Nova bio");
            assertThat(user.getAvatarUrl()).isEqualTo("http://new-avatar.jpg");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("keeps existing value when field is null in request")
        void shouldKeepExistingWhenNull() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, "joao");
            String originalBio = user.getBio();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);
            when(followRepository.countByFollowedId(id)).thenReturn(0L);
            when(followRepository.countByFollowerId(id)).thenReturn(0L);

            UpdateProfileRequest req = new UpdateProfileRequest(null, "http://new-avatar.jpg");
            userService.updateProfile(id, req);

            assertThat(user.getBio()).isEqualTo(originalBio);
            assertThat(user.getAvatarUrl()).isEqualTo("http://new-avatar.jpg");
        }
    }

    // ── getFollowers / getFollowing ───────────────────────────────────────────

    @Nested
    @DisplayName("getFollowers / getFollowing")
    class FollowLists {

        @Test
        @DisplayName("getFollowers returns correct user summaries")
        void shouldReturnFollowers() {
            UUID userId  = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();
            User owner    = buildUser(userId, "joao");
            User follower = buildUser(followerId, "maria");

            when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
            when(followRepository.findFollowerIdsByFollowedId(userId)).thenReturn(List.of(followerId));
            when(userRepository.findAllById(List.of(followerId))).thenReturn(List.of(follower));

            FollowListResponse result = userService.getFollowers(userId);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getUsers().get(0).getUsername()).isEqualTo("maria");
        }

        @Test
        @DisplayName("getFollowing returns correct user summaries")
        void shouldReturnFollowing() {
            UUID userId    = UUID.randomUUID();
            UUID followedId = UUID.randomUUID();
            User owner     = buildUser(userId, "joao");
            User followed  = buildUser(followedId, "carlos");

            when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
            when(followRepository.findFollowedIdsByFollowerId(userId)).thenReturn(List.of(followedId));
            when(userRepository.findAllById(List.of(followedId))).thenReturn(List.of(followed));

            FollowListResponse result = userService.getFollowing(userId);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getUsers().get(0).getUsername()).isEqualTo("carlos");
        }

        @Test
        @DisplayName("getFollowers returns empty list when user has no followers")
        void shouldReturnEmptyWhenNoFollowers() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId, "joao")));
            when(followRepository.findFollowerIdsByFollowedId(userId)).thenReturn(List.of());
            when(userRepository.findAllById(List.of())).thenReturn(List.of());

            FollowListResponse result = userService.getFollowers(userId);

            assertThat(result.getTotal()).isZero();
            assertThat(result.getUsers()).isEmpty();
        }
    }

    // ── follow / unfollow ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("follow / unfollow")
    class FollowActions {

        @Test
        @DisplayName("saves follow relationship when not yet following")
        void shouldSaveFollow() {
            UUID followerId = UUID.randomUUID();
            UUID followedId = UUID.randomUUID();
            when(userRepository.findById(followedId)).thenReturn(Optional.of(buildUser(followedId, "maria")));
            when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);

            userService.follow(followerId, followedId);

            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("is idempotent — does not save if already following")
        void shouldBeIdempotent() {
            UUID followerId = UUID.randomUUID();
            UUID followedId = UUID.randomUUID();
            when(userRepository.findById(followedId)).thenReturn(Optional.of(buildUser(followedId, "maria")));
            when(followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(true);

            userService.follow(followerId, followedId);

            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when user tries to follow themselves")
        void shouldThrowOnSelfFollow() {
            UUID id = UUID.randomUUID();

            assertThatThrownBy(() -> userService.follow(id, id))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("calls deleteByFollowerIdAndFollowedId on unfollow")
        void shouldUnfollow() {
            UUID followerId = UUID.randomUUID();
            UUID followedId = UUID.randomUUID();
            when(userRepository.findById(followedId)).thenReturn(Optional.of(buildUser(followedId, "maria")));

            userService.unfollow(followerId, followedId);

            verify(followRepository).deleteByFollowerIdAndFollowedId(followerId, followedId);
        }
    }
}