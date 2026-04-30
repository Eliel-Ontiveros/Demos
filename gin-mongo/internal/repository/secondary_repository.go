package repository

import (
	"context"
	"time"

	"github.com/eliel-ontiveros/gin-mongo/internal/model"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type SecondaryRepository struct {
	collection *mongo.Collection
}

func NewSecondaryRepository(db *mongo.Database) *SecondaryRepository {
	return &SecondaryRepository{
		collection: db.Collection(collectionName),
	}
}

func (r *SecondaryRepository) InsertOne(ctx context.Context, record *model.RecordDocument) (*model.RecordDocument, error) {
	ctx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()

	_, err := r.collection.InsertOne(ctx, record)
	if err != nil {
		return nil, err
	}
	return record, nil
}

func (r *SecondaryRepository) FindAll(ctx context.Context) ([]model.RecordDocument, error) {
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

func (r *SecondaryRepository) FindPaginated(ctx context.Context, limit, offset int) ([]model.RecordDocument, error) {
	ctx, cancel := context.WithTimeout(ctx, 10*time.Second)
	defer cancel()

	opts := options.Find().SetLimit(int64(limit)).SetSkip(int64(offset))
	cursor, err := r.collection.Find(ctx, bson.D{}, opts)
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

func (r *SecondaryRepository) InsertMany(ctx context.Context, records []interface{}) (int64, error) {
	ctx, cancel := context.WithTimeout(ctx, 30*time.Second)
	defer cancel()

	result, err := r.collection.InsertMany(ctx, records)
	if err != nil {
		return 0, err
	}
	return int64(len(result.InsertedIDs)), nil
}
