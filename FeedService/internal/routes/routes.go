package routes

import (
	"context"
	"net/http"
	"time"

	_ "github.com/Veketi/astreiagram/feed-service/docs"
	"github.com/Veketi/astreiagram/feed-service/internal/client"
	"github.com/Veketi/astreiagram/feed-service/internal/handler"
	"github.com/Veketi/astreiagram/feed-service/internal/middleware"
	"github.com/gin-gonic/gin"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/redis/go-redis/v9"

	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

func RegisterRoutes(
	r *gin.Engine,
	feedHanlder *handler.FeedHandler,
	userClient *client.UserClient,
	redisClient *redis.Client,
) {
	r.Use(middleware.Metrics())

	r.GET(
		"/health", 
		health(redisClient),
	)

	r.GET(
		"/metrics",
		gin.WrapH(promhttp.Handler()),
	)

	r.GET(
		"/swagger/*any",
		ginSwagger.WrapHandler(swaggerFiles.Handler),
	)

	api := r.Group(
		"/api",
	)

	api.GET(
		"/feed/:userId",
		middleware.AuthRequired(userClient),
		feedHanlder.GetFeed,
	)
}

func health(redisClient *redis.Client) gin.HandlerFunc {
	return func(c *gin.Context) {
		ctx, cancel := context.WithTimeout(c.Request.Context(), 2 * time.Second)
		defer cancel()

		if err := redisClient.Ping(ctx).Err(); err != nil {
			c.JSON(
				http.StatusServiceUnavailable,
				gin.H{
					"status": "down",
					"redis": "unreacheable",
			})
			return
		}
		
		c.JSON(
			http.StatusOK,
			gin.H{
				"status": "ok",
				"redis": "ok",
		})
	}
}
