package main

// @title AstreiaGram Feed Service API
// @version 1.0
// @description Feed Service of AstreiaGram, provides feed related funcionality for the application.
// @host localhost:8080
// @BasePath /

import (
	"context"
	"log"

	"github.com/Veketi/astreiagram/feed-service/internal/client"
	"github.com/Veketi/astreiagram/feed-service/internal/config"
	"github.com/Veketi/astreiagram/feed-service/internal/consumer"
	"github.com/Veketi/astreiagram/feed-service/internal/handler"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/routes"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

func main() {
	cfg := config.Load()

	rdb := redis.NewClient(&redis.Options{
		Addr: cfg.RedisAddr,
	})

	postClient := client.NewPostClient(cfg.PostClientUrl)
	userClient := client.NewUserClient(cfg.UserServiceUrl)

	feedRepo := repository.NewFeedRepository(rdb)
	feedHandler := handler.NewFeedHandler(feedRepo, postClient)

	followerService := client.NewUserClient(cfg.UserServiceUrl)

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
		userClient,
	)

	log.Println("Feed Service starting on :8080")
	if err := r.Run(":" + cfg.ServerPort); err != nil {
		log.Fatal(err)
	}
}
