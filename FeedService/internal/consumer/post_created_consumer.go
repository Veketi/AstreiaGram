package consumer

import (
	"context"
	"encoding/json"
	"log"
	//"time"

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
			//MaxWait: 100 * time.Millisecond,
		}),
		followerService: followerService,
	}
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

		followers, err := c.followerService.GetFollowers(
			ctx,
			event.AuthorID,
		)

		log.Printf(
			"Seguidores de %s: %+v",
			event.AuthorID,
			followers,
		)

		if err != nil {
			//log.Printf() error
			continue
		}

		err = c.repo.AddToFeed(
			ctx,
			event.AuthorID,
			event.PostID,
			event.CreatedAt,
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
		}


		if err != nil {
			log.Printf("erro redis: %v", err)
		}
	}
}
