package main

// @title AstreiaGram Feed Service API
// @version 1.0
// @description Microsserviço responsável por montar e retornar o feed cronológico dos usuários do AstreiaGram.
// @host localhost:8082
// @BasePath /
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
// @description Informe o token no formato: Bearer {token}

import (
	"context"
	"log/slog"
	"os"
	"os/signal"
	"syscall"

	"github.com/Veketi/astreiagram/feed-service/internal/client"
	"github.com/Veketi/astreiagram/feed-service/internal/config"
	"github.com/Veketi/astreiagram/feed-service/internal/consumer"
	"github.com/Veketi/astreiagram/feed-service/internal/handler"
	"github.com/Veketi/astreiagram/feed-service/internal/logger"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/routes"
	"github.com/gin-gonic/gin"
	"github.com/redis/go-redis/v9"
)

func main() {
	logger.Setup()

	cfg := config.Load()
	slog.Info("kafka broker config", "broker", cfg.KafkaBroker)

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	sig := make(chan os.Signal, 1)
	signal.Notify(sig, syscall.SIGINT, syscall.SIGTERM)

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


	slog.Info("starting kafka consumer",
		"topic", "post-created",
	)
	go consumer.Start(ctx)

	r := gin.Default()

	routes.RegisterRoutes(
		r,
		feedHandler,
		userClient,
		rdb,
	)

	slog.Info("Feed Service starting", "port", cfg.ServerPort)
	if err := r.Run(":" + cfg.ServerPort); err != nil {
		slog.Error("Error while starting the server", "error", err)
	}
}
