package consumer

import (
	"context"
	"errors"
	"testing"

	"github.com/Veketi/astreiagram/feed-service/internal/model"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/service"
	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
)

func setupConsumer(t *testing.T, followerService service.FollowerService) *PostCreatedConsumer {
	t.Helper()
	mini := miniredis.RunT(t)
	redisClient := redis.NewClient(&redis.Options{Addr: mini.Addr()})

	repo := repository.NewFeedRepository(redisClient)

	return &PostCreatedConsumer{
		repo: repo,
		followerService: followerService,
	}
}


func TestProcessEvent_AddsToAuthorAndFollowersFeeds(t *testing.T) {
	followerService := &mockFollowerService{}
	followerService.On("GetFollowers", mock.Anything, "author-1").
		Return([]string{"follower-1", "follower-2"}, nil)

	consumer := setupConsumer(t, followerService)

	event := model.PostCreatedEvent{
		AuthorID:  "author-1",
		PostID:    "post-1",
		CreatedAt: 100,
	}

	err := consumer.processEvent(context.Background(), event)
	require.NoError(t, err)

	authorFeed, err := consumer.repo.GetFeed(context.Background(), "author-1", 0, 10)
	require.NoError(t, err)
	require.Equal(t, []string{"post-1"}, authorFeed)

	follower1Feed, err := consumer.repo.GetFeed(context.Background(), "follower-1", 0, 10)
	require.NoError(t, err)
	require.Equal(t, []string{"post-1"}, follower1Feed)

	follower2Feed, err := consumer.repo.GetFeed(context.Background(), "follower-2", 0, 10)
	require.NoError(t, err)
	require.Equal(t, []string{"post-1"}, follower2Feed)

	followerService.AssertExpectations(t)
}

func TestProcessEvent_NoFollowers_StillAddsToAuthorFeed(t *testing.T) {
	followerService := &mockFollowerService{}
	followerService.On("GetFollowers", mock.Anything, "author-1").
		Return([]string{}, nil)

	consumer := setupConsumer(t, followerService)

	event := model.PostCreatedEvent{
		AuthorID:  "author-1",
		PostID:    "post-1",
		CreatedAt: 100,
	}

	err := consumer.processEvent(context.Background(), event)
	require.NoError(t, err)

	authorFeed, err := consumer.repo.GetFeed(context.Background(), "author-1", 0, 10)
	require.NoError(t, err)
	require.Equal(t, []string{"post-1"}, authorFeed)
}

func TestProcessEvent_GetFollowersFails_ReturnsError(t *testing.T) {
	followerService := &mockFollowerService{}
	followerService.On("GetFollowers", mock.Anything, "author-1").
		Return(nil, errors.New("user-service indisponível"))

	consumer := setupConsumer(t, followerService)

	event := model.PostCreatedEvent{
		AuthorID:  "author-1",
		PostID:    "post-1",
		CreatedAt: 100,
	}

	err := consumer.processEvent(context.Background(), event)
	require.Error(t, err)

	authorFeed, _ := consumer.repo.GetFeed(context.Background(), "author-1", 0, 10)
	require.Equal(t, []string{"post-1"}, authorFeed)
}
