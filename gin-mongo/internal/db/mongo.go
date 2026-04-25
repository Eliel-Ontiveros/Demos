package db

import (
	"context"
	"log"
	"time"

	"github.com/eliel-ontiveros/gin-mongo/internal/config"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type Clients struct {
	PrimaryClient   *mongo.Client
	SecondaryClient *mongo.Client
	PrimaryDB       string
	SecondaryDB     string
}

func Connect(cfg *config.Config) *Clients {
	primary := connectClient(cfg.MongoPrimaryURI)
	secondary := connectClient(cfg.MongoSecondaryURI)

	return &Clients{
		PrimaryClient:   primary,
		SecondaryClient: secondary,
		PrimaryDB:       cfg.MongoPrimaryDB,
		SecondaryDB:     cfg.MongoSecondaryDB,
	}
}

func connectClient(uri string) *mongo.Client {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))
	if err != nil {
		log.Fatalf("failed to connect to MongoDB at %s: %v", uri, err)
	}

	if err := client.Ping(ctx, nil); err != nil {
		log.Fatalf("failed to ping MongoDB at %s: %v", uri, err)
	}

	log.Printf("connected to MongoDB at %s", uri)
	return client
}

func Disconnect(clients *Clients) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := clients.PrimaryClient.Disconnect(ctx); err != nil {
		log.Printf("error disconnecting primary client: %v", err)
	}
	if err := clients.SecondaryClient.Disconnect(ctx); err != nil {
		log.Printf("error disconnecting secondary client: %v", err)
	}
}
