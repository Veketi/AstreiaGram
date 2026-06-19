package repository

import (
	"context"
	"fmt"
	"testing"

	"github.com/Veketi/astreiagram/feed-service/internal/model"
	"github.com/alicebob/miniredis/v2"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/require"
)

func createRepository(t *testing.T) (*FeedRepository, *redis.Client) {
	t.Helper()

	mini := miniredis.RunT(t)

	client := redis.NewClient(&redis.Options{
		Addr: mini.Addr(),
	})
	
	err := client.Ping(context.Background()).Err()
	require.NoError(t, err)
	
	return NewFeedRepository(client), client
}

func TestAddToFeed(t *testing.T) {
	repo, _ := createRepository(t)

	err := repo.AddToFeed(
		t.Context(),
		"1",
		"10",
		100,
	)

	require.NoError(t, err)

	posts, err := repo.GetFeed(
		t.Context(),
		"1",
		0,
		10,
	)

	require.NoError(t, err)

	require.Len(t, posts, 1)
	require.Equal(t, "10", posts[0])
}

func TestGetEmptyFeed(t *testing.T) {
	repo, _ := createRepository(t)
	userID := "1"
	offset := 0
	limit := 50
	result, err := repo.GetFeed(t.Context(), userID, int64(offset), int64(limit))

	require.NoError(t, err)
    require.Empty(t, result)
}

func TestFeedOrder(t *testing.T) {
	repo, _ := createRepository(t)

	posts := []model.PostCreatedEvent{
		{AuthorID: "1", PostID: "1", CreatedAt: 10},
		{AuthorID: "1", PostID: "2", CreatedAt: 20},
		{AuthorID: "1", PostID: "3", CreatedAt: 30},
	}

	for _, p := range posts{
		err := repo.AddToFeed(
			t.Context(),
			p.AuthorID,
			p.PostID,
			p.CreatedAt,
		)
		require.NoError(t, err)
	}
	userID := "1"
	page := 1
	limit := 50
	offset := (page - 1) * limit
	result, err := repo.GetFeed(t.Context(), userID, int64(offset), int64(limit))

	require.NoError(t, err)
	require.Len(t, result, 3)
	require.Equal(t, []string{"3", "2", "1"}, result)
}

func TestFeedPagination(t *testing.T) {
	repo, _ := createRepository(t)

	for i := range 100 {
		err := repo.AddToFeed(
			t.Context(),
			"1",
			fmt.Sprintf("%d", i),
			int64(i),
		)
		require.NoError(t, err)
	}
	userID := "1"
	page := 2
	limit := 10
	offset := (page - 1) * limit
	result, err := repo.GetFeed(t.Context(), userID, int64(offset), int64(limit))

	require.NoError(t, err)
	require.Len(t, result, 10)
	require.Equal(t, []string{"89","88","87","86","85","84","83","82","81","80"}, result)
}

func TestFeedMaxSize(t *testing.T) {
	repo, _ := createRepository(t)

	for i := range 1100 {
		err := repo.AddToFeed(
			t.Context(),
			"1",
			fmt.Sprintf("%d", i),
			int64(i),
		)
		require.NoError(t, err)
	}
	userID := "1"
	page := 1
	limit := 1000
	offset := (page - 1) * limit
	result, err := repo.GetFeed(t.Context(), userID, int64(offset), int64(limit))

	require.NoError(t, err)
	require.Len(t, result, 1000)
	require.Equal(t, "1099", result[0])
	require.Equal(t, "100", result[999])
}

func TestFeedIsolation(t *testing.T) {
	repo, _ := createRepository(t)

	firstUserID :=  "1"
	firstPostID := "1"

	secondUserID := "2"
	secondPostID := "1"

	repo.AddToFeed(t.Context(), firstUserID, firstPostID, 100)
	repo.AddToFeed(t.Context(), secondUserID, secondPostID, 200)

	offset := 0
	limit := 50
	firstResult, _ := repo.GetFeed(t.Context(), firstUserID, int64(offset), int64(limit))
	secondResult, _ := repo.GetFeed(t.Context(), secondUserID, int64(offset), int64(limit))

	require.Len(t, firstResult, 1)
	require.Len(t, secondResult, 1)
	
	require.Equal(t, firstPostID, firstResult[0])
	require.Equal(t, secondPostID, secondResult[0])
}
