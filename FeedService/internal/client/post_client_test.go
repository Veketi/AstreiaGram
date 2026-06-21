package client

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/stretchr/testify/require"
)

func TestGetPosts_ReturnsEmptySlice_WhenNoIDs(t *testing.T) {
	client := NewPostClient("http://unused")

	posts, err := client.GetPosts(context.Background(), []string{})

	require.NoError(t, err)
	require.Empty(t, posts)
}

func TestGetPosts_ReturnsPosts_OnSuccess(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		require.Equal(t, "/api/posts", r.URL.Path)
		require.Equal(t, "id1,id2", r.URL.Query().Get("ids"))

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode([]dto.PostResponse{
			{ID: "id1"},
			{ID: "id2"},
		})
	}))
	defer server.Close()

	client := NewPostClient(server.URL)

	posts, err := client.GetPosts(context.Background(), []string{"id1", "id2"})

	require.NoError(t, err)
	require.Len(t, posts, 2)
}

func TestGetPosts_ReturnsError_WhenServerFails(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
	}))
	defer server.Close()

	client := NewPostClient(server.URL)

	_, err := client.GetPosts(context.Background(), []string{"id1"})

	require.Error(t, err)
}
