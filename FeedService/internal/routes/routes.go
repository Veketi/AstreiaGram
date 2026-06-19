package routes

import (
	"github.com/Veketi/astreiagram/feed-service/internal/handler"
	_ "github.com/Veketi/astreiagram/feed-service/docs"
	"github.com/gin-gonic/gin"

	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

func RegisterRoutes(
	r *gin.Engine,
	feedHanlder *handler.FeedHandler,
) {
	r.GET(
		"/health", 
		health,
	)

	r.GET(
		"/feed/:userId",
		feedHanlder.GetFeed,
	)

	r.GET(
		"/swagger/*any",
		ginSwagger.WrapHandler(swaggerFiles.Handler),
	)
}

func health(c *gin.Context) {
	c.JSON(200, gin.H{"status": "ok"})
}
