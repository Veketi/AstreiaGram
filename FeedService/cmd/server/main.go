package main

// @title AstreiaGram Feed Service API
// @version 1.0
// @description Feed Service of AstreiaGram, provides feed related funcionality for the application.
// @host localhost:8080
// @BasePath /

import (
	"context"
	"log"

	"github.com/Veketi/astreiagram/feed-service/internal/config"
	"github.com/Veketi/astreiagram/feed-service/internal/consumer"
	"github.com/Veketi/astreiagram/feed-service/internal/handler"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/routes"
	"github.com/Veketi/astreiagram/feed-service/internal/service"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

func main() {
	cfg := config.Load()

	rdb := redis.NewClient(&redis.Options{
		Addr: cfg.RedisAddr,
	})

	feedRepo := repository.NewFeedRepository(rdb)
	feedHandler := handler.NewFeedHandler(feedRepo)

	followerService := service.NewMockFollowerService()

	consumer := consumer.NewPostCreatedConsumer(
		[]string{cfg.KafkaBroker},
		feedRepo,
		followerService,
	)

	go consumer.Start(context.Background())

	r := gin.Default()

	routes.RegisterRoutes(
		r,
		feedHandler,
	)

	log.Println("Feed Service starting on :8080")
	if err := r.Run(":8080"); err != nil {
		log.Fatal(err)
	}
}
