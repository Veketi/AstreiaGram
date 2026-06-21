package handler

import (
	"context"
	"net/http"
	"strconv"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/util"
	"github.com/gin-gonic/gin"
)

type PostGetter interface {
	GetPosts(ctx context.Context, ids []string) ([]dto.PostResponse, error)
}

type FeedHandler struct {
	repo *repository.FeedRepository
	postClient PostGetter
}

func NewFeedHandler(repo *repository.FeedRepository, postClient PostGetter) *FeedHandler {
	return &FeedHandler{
		repo: repo,
		postClient: postClient,
	}
}

// GetFeed retorna o feed de um usuário.
//
// @Summary Procura por um feed.
// @Description Retorna posts de um feed de algum usuário.
// @Tag Feed
// @Produce json
// @Param userId path string true "ID do usuário"
// @Param page query int false "Número da página" default(1)
// @Param limit query int false "Número de posts por página" default(50)
// @Success 200 {object} dto.FeedResponse
// @Failure 400 {object} dto.ErrorResponse
// @Failure 500 {object} dto.ErrorResponse
// @Router /api/feed/{userId} [get]
func (h *FeedHandler) GetFeed(c *gin.Context) {
	userID := c.Param("userId")

	authenticatedUserID, exists := c.Get("userId")
	if !exists {
		c.JSON(http.StatusForbidden, dto.ErrorResponse{Error: "You don't have permissions for this feed."})
		return
	}

	authenticatedUserIDStr, ok := authenticatedUserID.(string)
	if !ok || authenticatedUserIDStr != userID {
		c.JSON(http.StatusForbidden, dto.ErrorResponse{Error: "You don't have permissions for this feed."})
		return
	}

	page, err := strconv.ParseInt(
		c.DefaultQuery("page", "1"),
		10,
		64,
	)

	if err != nil {
		c.JSON(
			http.StatusBadRequest,
			dto.ErrorResponse{
				Error: "Invalid page.",
			},
		)
		return
	}

	limit, err := strconv.ParseInt(
		c.DefaultQuery("limit", "50"),
		10,
		64,
	)

	if err != nil {
		c.JSON(
			http.StatusBadRequest,
			dto.ErrorResponse{
				Error: "Invalid limit.",
			},
		)
		return
	}

	offset := (page - 1) * limit

	postIDs, err := h.repo.GetFeed(c.Request.Context(), userID, offset, limit)

	if err != nil {
		c.JSON(
			http.StatusInternalServerError, 
			dto.ErrorResponse{
				Error: err.Error(),
			},
		)
		return
	}

	posts, err := h.postClient.GetPosts(c.Request.Context(), postIDs)

	if err != nil {

		c.JSON(
			http.StatusInternalServerError, 
			dto.ErrorResponse{
				Error: err.Error(),
			},
		)
		return
	}

	posts = util.ReorderPosts(postIDs, posts)

	response := dto.FeedResponse{
		UserID: userID,
		Posts: posts,
		Page: page,
		Limit: limit,
	}

	c.JSON(
		http.StatusOK,
		response,
	)
}
