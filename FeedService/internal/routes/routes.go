package routes

import (
	_ "github.com/Veketi/astreiagram/feed-service/docs"
	"github.com/Veketi/astreiagram/feed-service/internal/client"
	"github.com/Veketi/astreiagram/feed-service/internal/handler"
	"github.com/Veketi/astreiagram/feed-service/internal/middleware"
	"github.com/gin-gonic/gin"

	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

func RegisterRoutes(
	r *gin.Engine,
	feedHanlder *handler.FeedHandler,
	userClient *client.UserClient,
) {
	api := r.Group(
		"/api",
	)

	api.GET(
		"/health", 
		health,
	)

	api.GET(
		"/feed/:userId",
		middleware.AuthRequired(userClient),
		feedHanlder.GetFeed,
	)

	api.GET(
		"/swagger/*any",
		ginSwagger.WrapHandler(swaggerFiles.Handler),
	)
}

func health(c *gin.Context) {
	c.JSON(200, gin.H{"status": "ok"})
}
