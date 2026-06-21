package handler

import (
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"
	"encoding/json"
	"context"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/alicebob/miniredis/v2"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
)

func setupFeedHandler(t *testing.T, postGetter *mockPostGetter) *FeedHandler {
	t.Helper()

	mini := miniredis.RunT(t)
	redisClient := redis.NewClient(&redis.Options{Addr: mini.Addr()})
	repo := repository.NewFeedRepository(redisClient)

	return NewFeedHandler(repo, postGetter)
}

func setupTestContext(method, url string, authenticatedUserID string) (*gin.Context, *httptest.ResponseRecorder) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	req := httptest.NewRequest(method, url, nil)
	c.Request = req

	if authenticatedUserID != "" {
		c.Set("userId", authenticatedUserID)
	}

	return c, w
}

func TestGetFeed_ReturnsForbidden_WhenNotAuthenticated(t *testing.T) {
	postGetter := &mockPostGetter{}
	handler := setupFeedHandler(t, postGetter)

	c, w := setupTestContext(http.MethodGet, "/feed/user-1", "")
	c.Params = gin.Params{{Key: "userId", Value: "user-1"}}

	handler.GetFeed(c)

	require.Equal(t, http.StatusForbidden, w.Code)
}

func TestGetFeed_ReturnsForbidden_WhenDifferentUser(t *testing.T) {
	postGetter := &mockPostGetter{}
	handler := setupFeedHandler(t, postGetter)

	c, w := setupTestContext(http.MethodGet, "/feed/user-1", "user-2")
	c.Params = gin.Params{{Key: "userId", Value: "user-1"}}

	handler.GetFeed(c)

	require.Equal(t, http.StatusForbidden, w.Code)
}

func TestGetFeed_ReturnsEmptyFeed_WhenNoPosts(t *testing.T) {
	postGetter := &mockPostGetter{}
	postGetter.On("GetPosts", mock.Anything, []string{}).
		Return([]dto.PostResponse{}, nil)

	handler := setupFeedHandler(t, postGetter)

	c, w := setupTestContext(http.MethodGet, "/feed/user-1", "user-1")
	c.Params = gin.Params{{Key: "userId", Value: "user-1"}}

	handler.GetFeed(c)

	require.Equal(t, http.StatusOK, w.Code)
}

func TestGetFeed_ReturnsBadRequest_WhenPageIsInvalid(t *testing.T) {
	postGetter := &mockPostGetter{}
	handler := setupFeedHandler(t, postGetter)

	c, w := setupTestContext(http.MethodGet, "/feed/user-1?page=abc", "user-1")
	c.Params = gin.Params{{Key: "userId", Value: "user-1"}}

	handler.GetFeed(c)

	require.Equal(t, http.StatusBadRequest, w.Code)
}

func TestGetFeed_ReturnsInternalServerError_WhenPostClientFails(t *testing.T) {
	postGetter := &mockPostGetter{}
	postGetter.On("GetPosts", mock.Anything, []string{}).
		Return(nil, errors.New("post-service indisponível"))

	handler := setupFeedHandler(t, postGetter)

	c, w := setupTestContext(http.MethodGet, "/feed/user-1", "user-1")
	c.Params = gin.Params{{Key: "userId", Value: "user-1"}}

	handler.GetFeed(c)

	require.Equal(t, http.StatusInternalServerError, w.Code)
}


func TestGetFeed_ReturnsPostsInCorrectOrder(t *testing.T) {
	postGetter := &mockPostGetter{}

	mini := miniredis.RunT(t)
	redisClient := redis.NewClient(&redis.Options{Addr: mini.Addr()})
	repo := repository.NewFeedRepository(redisClient)

	// popula o feed no Redis, mais recente por último (CreatedAt crescente)
	ctx := context.Background()
	require.NoError(t, repo.AddToFeed(ctx, "user-1", "post-1", 100))
	require.NoError(t, repo.AddToFeed(ctx, "user-1", "post-2", 200))
	require.NoError(t, repo.AddToFeed(ctx, "user-1", "post-3", 300))

	// o Post Service devolve fora de ordem de propósito (simula comportamento real do Mongo)
	postGetter.On("GetPosts", mock.Anything, []string{"post-3", "post-2", "post-1"}).
		Return([]dto.PostResponse{
			{ID: "post-1", Caption: "primeiro"},
			{ID: "post-3", Caption: "terceiro"},
			{ID: "post-2", Caption: "segundo"},
		}, nil)

	handler := NewFeedHandler(repo, postGetter)

	c, w := setupTestContext(http.MethodGet, "/feed/user-1", "user-1")
	c.Params = gin.Params{{Key: "userId", Value: "user-1"}}

	handler.GetFeed(c)

	require.Equal(t, http.StatusOK, w.Code)

	var response dto.FeedResponse
	require.NoError(t, json.Unmarshal(w.Body.Bytes(), &response))

	require.Len(t, response.Posts, 3)
	require.Equal(t, "post-3", response.Posts[0].ID)
	require.Equal(t, "post-2", response.Posts[1].ID)
	require.Equal(t, "post-1", response.Posts[2].ID)
}
