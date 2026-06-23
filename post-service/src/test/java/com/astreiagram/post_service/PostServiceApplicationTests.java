package com.astreiagram.post_service;

import com.astreiagram.post_service.client.UserClient;
import com.astreiagram.post_service.dto.CreatePostRequest;
import com.astreiagram.post_service.event.PostCreatedEvent;
import com.astreiagram.post_service.event.PostEventPublisher;
import com.astreiagram.post_service.exception.CommentNotFoundException;
import com.astreiagram.post_service.exception.PostNotFoundException;
import com.astreiagram.post_service.exception.UnauthorizedException;
import com.astreiagram.post_service.exception.UserNotFoundException;
import com.astreiagram.post_service.model.Comment;
import com.astreiagram.post_service.model.Post;
import com.astreiagram.post_service.repository.PostRepository;
import com.astreiagram.post_service.service.PostService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    private PostRepository postRepository;
    private UserClient userClient;
    private PostEventPublisher eventPublisher;
    private PostService postService;

    // Post é um record — construção direta pelo construtor canônico
    private Post buildPost(String id, String userId) {
        return new Post(
            id,
            userId,
            "http://img.test/foto.jpg",
            "legenda",
            LocalDateTime.now(),
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        userClient     = mock(UserClient.class);
        eventPublisher = mock(PostEventPublisher.class);
        postService    = new PostService(postRepository, userClient, eventPublisher);
    }

    // ── createPost ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("saves the post and publishes Kafka event")
        void shouldSaveAndPublishEvent() {
            CreatePostRequest req = new CreatePostRequest("http://img.test/foto.jpg", "minha legenda");
            Post saved = buildPost("post-1", "user-1");
            when(postRepository.save(any())).thenReturn(saved);

            Post result = postService.createPost("user-1", req);

            assertThat(result.id()).isEqualTo("post-1");
            assertThat(result.userId()).isEqualTo("user-1");

            ArgumentCaptor<PostCreatedEvent> captor = ArgumentCaptor.forClass(PostCreatedEvent.class);
            verify(eventPublisher).publishPostCreated(captor.capture());
            assertThat(captor.getValue().postId()).isEqualTo("post-1");
            assertThat(captor.getValue().authorId()).isEqualTo("user-1");
        }
    }

    // ── getPostById ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPostById")
    class GetPostById {

        @Test
        @DisplayName("returns post when found")
        void shouldReturnPost() {
            Post post = buildPost("post-1", "user-1");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            Post result = postService.getPostById("post-1");

            assertThat(result.id()).isEqualTo("post-1");
        }

        @Test
        @DisplayName("throws PostNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(postRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostById("missing"))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    // ── getPostsByUserId ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPostsByUserId")
    class GetPostsByUserId {

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userClient.userExists("ghost")).thenReturn(false);

            assertThatThrownBy(() -> postService.getPostsByUserId("ghost", Pageable.unpaged()))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("delegates to repository when user exists")
        void shouldDelegateToRepo() {
            when(userClient.userExists("user-1")).thenReturn(true);
            when(postRepository.findByUserId(eq("user-1"), any())).thenReturn(Page.empty());

            postService.getPostsByUserId("user-1", Pageable.unpaged());

            verify(postRepository).findByUserId(eq("user-1"), any());
        }
    }

    // ── deletePost ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("deletes when requester is the author")
        void shouldDeleteWhenOwner() {
            Post post = buildPost("post-1", "user-1");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            postService.deletePost("post-1", "user-1");

            verify(postRepository).deleteById("post-1");
        }

        @Test
        @DisplayName("throws UnauthorizedException when requester is not the author")
        void shouldThrowWhenNotOwner() {
            Post post = buildPost("post-1", "user-1");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deletePost("post-1", "user-2"))
                    .isInstanceOf(UnauthorizedException.class);

            verify(postRepository, never()).deleteById(any());
        }
    }

    // ── likePost / unlikePost / getLikes ──────────────────────────────────────

    @Nested
    @DisplayName("likePost / unlikePost / getLikes")
    class Likes {

        @Test
        @DisplayName("adds userId to likes (does not duplicate on second call)")
        void shouldAddLikeOnce() {
            Post post = buildPost("post-1", "user-1");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postService.likePost("post-1", "user-2");
            postService.likePost("post-1", "user-2"); // idempotente

            assertThat(post.likedByUserIds()).containsExactly("user-2");
        }

        @Test
        @DisplayName("removes like correctly")
        void shouldRemoveLike() {
            Post post = buildPost("post-1", "user-1");
            post.likedByUserIds().add("user-2");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postService.unlikePost("post-1", "user-2");

            assertThat(post.likedByUserIds()).isEmpty();
        }

        @Test
        @DisplayName("getLikes paginates correctly")
        void shouldPaginateLikes() {
            Post post = buildPost("post-1", "user-1");
            post.likedByUserIds().addAll(List.of("u1", "u2", "u3", "u4", "u5"));
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            List<String> page0 = postService.getLikes("post-1", 0, 3);
            List<String> page1 = postService.getLikes("post-1", 1, 3);

            assertThat(page0).containsExactly("u1", "u2", "u3");
            assertThat(page1).containsExactly("u4", "u5");
        }

        @Test
        @DisplayName("getLikes returns empty list for out-of-bounds page")
        void shouldReturnEmptyForOutOfBoundsPage() {
            Post post = buildPost("post-1", "user-1");
            post.likedByUserIds().addAll(List.of("u1", "u2"));
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            List<String> result = postService.getLikes("post-1", 5, 10);

            assertThat(result).isEmpty();
        }
    }

    // ── addComment / getComments / deleteComment ──────────────────────────────

    @Nested
    @DisplayName("addComment / getComments / deleteComment")
    class Comments {

        @Test
        @DisplayName("adds a comment with auto-generated UUID")
        void shouldAddComment() {
            Post post = buildPost("post-1", "user-1");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postService.addComment("post-1", "user-2", "Great photo!");

            assertThat(post.comments()).hasSize(1);
            assertThat(post.comments().get(0).content()).isEqualTo("Great photo!");
            assertThat(post.comments().get(0).userId()).isEqualTo("user-2");
            // Comment record gera UUID automaticamente no compact constructor
            assertThat(post.comments().get(0).id()).isNotNull();
        }

        @Test
        @DisplayName("getComments paginates correctly")
        void shouldPaginateComments() {
            Post post = buildPost("post-1", "user-1");
            for (int i = 1; i <= 5; i++) {
                post.comments().add(new Comment("id-" + i, "user-x", "comment " + i, LocalDateTime.now()));
            }
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            List<Comment> page0 = postService.getComments("post-1", 0, 3);
            List<Comment> page1 = postService.getComments("post-1", 1, 3);

            assertThat(page0).hasSize(3);
            assertThat(page1).hasSize(2);
        }

        @Test
        @DisplayName("deletes comment when requester is the author")
        void shouldDeleteCommentWhenOwner() {
            Post post = buildPost("post-1", "user-1");
            post.comments().add(new Comment("cid-1", "user-2", "hi", LocalDateTime.now()));
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));
            when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            postService.deleteComment("post-1", "cid-1", "user-2");

            assertThat(post.comments()).isEmpty();
        }

        @Test
        @DisplayName("throws UnauthorizedException when deleting another user's comment")
        void shouldThrowWhenDeletingOtherUsersComment() {
            Post post = buildPost("post-1", "user-1");
            post.comments().add(new Comment("cid-1", "user-2", "hi", LocalDateTime.now()));
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deleteComment("post-1", "cid-1", "intruder"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("throws CommentNotFoundException when comment does not exist")
        void shouldThrowWhenCommentNotFound() {
            Post post = buildPost("post-1", "user-1");
            when(postRepository.findById("post-1")).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deleteComment("post-1", "missing-id", "user-2"))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }
}