package consumer

import (
	"context"
	"encoding/json"
	"fmt"
	"log/slog"

	//"time"

	"github.com/Veketi/astreiagram/feed-service/internal/metrics"
	"github.com/Veketi/astreiagram/feed-service/internal/model"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/service"
	"github.com/segmentio/kafka-go"
)

type PostCreatedConsumer struct {
	reader *kafka.Reader
	repo *repository.FeedRepository
	followerService service.FollowerService
}

func NewPostCreatedConsumer(
	brokers []string,
	repo *repository.FeedRepository,
	followerService service.FollowerService,
) *PostCreatedConsumer {
	return &PostCreatedConsumer{
		repo: repo,
		reader: kafka.NewReader(kafka.ReaderConfig{
			Brokers: brokers,
			Topic: "post-created",
			GroupID: "feed-service",
			MinBytes: 1,
			MaxBytes: 10e6,
		}),
		followerService: followerService,
	}
}

func (c *PostCreatedConsumer) processEvent(
	ctx context.Context,
	event model.PostCreatedEvent,
) error {
	err := c.repo.AddToFeed(
		ctx,
		event.AuthorID,
		event.PostID,
		event.CreatedAt,
	)

	if err != nil {
		metrics.EventsProcessed.WithLabelValues("error").Inc()
		return fmt.Errorf(
			"Error while adding at adding the post to the user's own feed, user %s: %w", 
			event.AuthorID,
			err,
		)
	}

	followers, err := c.followerService.GetFollowers(
		ctx,
		event.AuthorID,
	)

	if err != nil {
		metrics.EventsProcessed.WithLabelValues("error").Inc()
		return fmt.Errorf(
			"error while getting the followers for user %s: %w", 
			event.AuthorID,
			err,
		)
	}

	slog.Info(
		"user followers",
		"userId", event.AuthorID,
		"followerIds", followers,
	)


	for _, followerID := range followers {
		err = c.repo.AddToFeed(
			ctx,
			followerID,
			event.PostID,
			event.CreatedAt,
		)

		if err != nil {
			slog.Error(
				"error adding post to the follower's feed",
				"followerId", followerID,
				"postId", event.PostID,
				"error", err,
			)
		}
	}

	metrics.EventsProcessed.WithLabelValues("success").Inc()
	return nil
}

func (c *PostCreatedConsumer) Start(ctx context.Context) {
	slog.Info(
		"Started consuming events",
	)
	for {
		slog.Info("about to read kafka message")
		msg, err := c.reader.ReadMessage(ctx)
		slog.Info("message received RAW",
			"value", string(msg.Value),
		)
		slog.Info("kafka returned message", "err", err)

		if err != nil {
			slog.Error(
				"error reading message from kafka",
				"messageValue", string(msg.Value),
				"error", err,
			)
			continue
		}

		var event model.PostCreatedEvent

		if err := json.Unmarshal(msg.Value, &event); err != nil {
			slog.Error(
				"error unmarshaling event",
				"rawMessage", string(msg.Value),
			)
			continue
		}

		slog.Info(
			"event received",
			"postId", event.PostID,
			"authorId", event.AuthorID,
		)

		if err := c.processEvent(ctx, event); err != nil {
			slog.Error(
				"error processing event",
				"postId", event.PostID,
				"error", err,
			)
		}
	}
}
