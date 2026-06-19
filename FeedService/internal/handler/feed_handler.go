package handler

import (
	"net/http"
	"strconv"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/gin-gonic/gin"
)

type FeedHandler struct {
	repo *repository.FeedRepository
}

func NewFeedHandler(repo *repository.FeedRepository) *FeedHandler {
	return &FeedHandler{repo: repo}
}

// GetFeed returns the feed of one user.
//
// @Summary Searches for a feed.
// @Description Returns posts from a feed of some user.
// @Tag Feed
// @Produce json
// @Param userId path string true "ID of the user"
// @Param page query int false "Number of the page" default(1)
// @Param limit query int false "Number of post per page" default(50)
// @Success 200 {object} dto.FeedResponse
// @Failure 400 {object} dto.ErrorResponse
// @Failure 500 {object} dto.ErrorResponse
// @Router /feed/{userId} [get]
func (h *FeedHandler) GetFeed(c *gin.Context) {
	userID := c.Param("userId")

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

	posts, err := h.repo.GetFeed(c.Request.Context(), userID, offset, limit)

	if err != nil {
		c.JSON(
			http.StatusInternalServerError, 
			dto.ErrorResponse{
				Error: err.Error(),
			},
		)
		return
	}

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
