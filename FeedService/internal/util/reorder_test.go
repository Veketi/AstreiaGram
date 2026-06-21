package util

import (
	"testing"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/stretchr/testify/require"
)

func TestReorderPosts_MatchesGivenOrder(t *testing.T) {
	postIDs := []string{"3", "2", "1"}
	posts := []dto.PostResponse{
		{ID: "1"},
		{ID: "2"},
		{ID: "3"},
	}

	result := ReorderPosts(postIDs, posts)

	require.Len(t, result, 3)
	require.Equal(t, "3", result[0].ID)
	require.Equal(t, "2", result[1].ID)
	require.Equal(t, "1", result[2].ID)
}

func TestReorderPosts_EmptyPostIDs(t *testing.T) {
	result := ReorderPosts([]string{}, []dto.PostResponse{{ID: "1"}})

	require.Empty(t, result)
}

func TestReorderPosts_EmptyPosts(t *testing.T) {
	result := ReorderPosts([]string{"1", "2"}, []dto.PostResponse{})

	require.Empty(t, result)
}

func TestReorderPosts_PostIDMissingFromResults(t *testing.T) {
	// post "2" foi deletado entre o Redis e a busca no Mongo
	postIDs := []string{"3", "2", "1"}
	posts := []dto.PostResponse{
		{ID: "1"},
		{ID: "3"},
	}

	result := ReorderPosts(postIDs, posts)

	require.Len(t, result, 2)
	require.Equal(t, "3", result[0].ID)
	require.Equal(t, "1", result[1].ID)
}

func TestReorderPosts_DuplicateIDsInPostIDs(t *testing.T) {
	postIDs := []string{"1", "1", "2"}
	posts := []dto.PostResponse{
		{ID: "1"},
		{ID: "2"},
	}

	result := ReorderPosts(postIDs, posts)

	require.Len(t, result, 3)
	require.Equal(t, []string{"1", "1", "2"}, []string{result[0].ID, result[1].ID, result[2].ID})
}
