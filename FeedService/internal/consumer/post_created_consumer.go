package consumer

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	//"time"

	"github.com/Veketi/astreiagram/feed-service/internal/model"
	"github.com/Veketi/astreiagram/feed-service/internal/repository"
	"github.com/Veketi/astreiagram/feed-service/internal/service"
	"github.com/Veketi/astreiagram/feed-service/internal/metrics"
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
			//MaxWait: 100 * time.Millisecond,
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
		return fmt.Errorf("Erro ao adicionar ao próprio feed: %v", err)
	}

	followers, err := c.followerService.GetFollowers(
		ctx,
		event.AuthorID,
	)

	if err != nil {
		metrics.EventsProcessed.WithLabelValues("error").Inc()
		return fmt.Errorf("erro ao buscar seguidores: %v", err)
	}

	log.Printf(
		"Seguidores de %s: %+v",
		event.AuthorID,
		followers,
	)

	for _, followerID := range followers {
		log.Printf(
			"Adicionando post ao feed do usuário %s",
			followerID,
		)

		err = c.repo.AddToFeed(
			ctx,
			followerID,
			event.PostID,
			event.CreatedAt,
		)

		if err != nil {
			log.Printf("erro redis: %v", err)
		}

	}

	metrics.EventsProcessed.WithLabelValues("success").Inc()
	return nil
}

func (c *PostCreatedConsumer) Start(ctx context.Context) {
	//c.reader.SetOffset(kafka.LastOffset)
	for {
		log.Print("Aguardando evento...")

		msg, err := c.reader.ReadMessage(ctx)

		if err != nil {
			log.Printf("erro kafka: %v", err)
			continue
		}

		log.Printf(
			"Evento recebido: %v",
			string(msg.Value),
		)

		var event model.PostCreatedEvent

		if err := json.Unmarshal(msg.Value, &event); err != nil {
			log.Printf("erro json: %v", err)
			continue
		}

		log.Printf("novo post recebido: %+v", event)

		if err := c.processEvent(ctx, event); err != nil {
			log.Printf("erro ao processar evento: %v", err)
		}
	}
}
