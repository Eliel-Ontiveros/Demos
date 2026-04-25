package model

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type RecordDocument struct {
	ID        primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	Tenant    string             `bson:"tenant" json:"tenant"`
	Name      string             `bson:"name" json:"name"`
	Value     string             `bson:"value" json:"value"`
	CreatedAt time.Time          `bson:"createdAt" json:"createdAt"`
}
