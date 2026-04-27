package repository

import (
	"context"
	"time"

	"github.com/eliel-ontiveros/gin-mongo/internal/model"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

const collectionName = "records"

type PrimaryRepository struct {
	collection *mongo.Collection
}

func NewPrimaryRepository(db *mongo.Database) *PrimaryRepository {
	return &PrimaryRepository{
		collection: db.Collection(collectionName),
	}
}

func (r *PrimaryRepository) InsertOne(ctx context.Context, record *model.RecordDocument) (*model.RecordDocument, error) {
	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	_, err := r.collection.InsertOne(ctx, record)
	if err != nil {
		return nil, err
	}
	return record, nil
}

func (r *PrimaryRepository) FindAll(ctx context.Context) ([]model.RecordDocument, error) {
	ctx, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()

	cursor, err := r.collection.Find(ctx, bson.D{})
	if err != nil {
		return nil, err
	}
	defer cursor.Close(ctx)

	var records []model.RecordDocument
	if err := cursor.All(ctx, &records); err != nil {
		return nil, err
	}
	return records, nil
}

func (r *PrimaryRepository) InsertMany(ctx context.Context, records []interface{}) (int64, error) {
	ctx, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()

	result, err := r.collection.InsertMany(ctx, records)
	if err != nil {
		return 0, err
	}
	return int64(len(result.InsertedIDs)), nil
}
