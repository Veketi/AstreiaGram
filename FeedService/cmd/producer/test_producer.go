package main

import (
	"context"
	"fmt"
	"log"
	"time"

	"github.com/segmentio/kafka-go"
)

func main() {
	log.Print(
		"Enviando o evento",
	)

	start := time.Now()

	writer := &kafka.Writer{
		Addr: kafka.TCP("localhost:9092"),
		Topic: "post-created",
	}

	defer writer.Close()


	for i := range 200 {
		err := writer.WriteMessages(
			context.Background(),
			kafka.Message{
				Value: []byte(fmt.Sprintf(`{
					"postId":"%d",
					"authorId":"5",
					"createdAt":%d
				}`, i, (i * 100))),
			},
		)

		if err != nil {
			log.Fatal(err)
			continue
		}
	}

	log.Printf(
		"Envio concluído em %v",
		time.Since(start),
	)
	
	log.Printf(
		"Evento enviado: %v",
		time.Now(),
	)
}
