package service

import (
	"context"
	"fmt"
	"time"

	"github.com/eliel-ontiveros/gin-mongo/internal/model"
	"github.com/eliel-ontiveros/gin-mongo/internal/repository"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

var validTenants = map[string]bool{
	"tenant1": true,
	"tenant2": true,
	"tenant3": true,
}

var tenants = []string{"tenant1", "tenant2", "tenant3"}

const (
	seedTotal     = 100_000
	seedBatchSize = 1_000
)

type RecordService struct {
	primaryRepo   *repository.PrimaryRepository
	secondaryRepo *repository.SecondaryRepository
}

func NewRecordService(primary *repository.PrimaryRepository, secondary *repository.SecondaryRepository) *RecordService {
	return &RecordService{
		primaryRepo:   primary,
		secondaryRepo: secondary,
	}
}

func (s *RecordService) ValidateTenant(tenant string) error {
	if !validTenants[tenant] {
		return fmt.Errorf("missing or invalid X-Tenant-ID header. Allowed: tenant1, tenant2, tenant3")
	}
	return nil
}

func (s *RecordService) CreateRecord(ctx context.Context, tenant, name, value string) (map[string]interface{}, error) {
	now := time.Now()

	primary := &model.RecordDocument{
		ID:        primitive.NewObjectID(),
		Tenant:    tenant,
		Name:      name,
		Value:     value,
		CreatedAt: now,
	}
	secondary := &model.RecordDocument{
		ID:        primitive.NewObjectID(),
		Tenant:    tenant,
		Name:      name,
		Value:     value,
		CreatedAt: now,
	}

	primaryResult, err := s.primaryRepo.InsertOne(ctx, primary)
	if err != nil {
		return nil, fmt.Errorf("primary insert failed: %w", err)
	}

	secondaryResult, err := s.secondaryRepo.InsertOne(ctx, secondary)
	if err != nil {
		return nil, fmt.Errorf("secondary insert failed: %w", err)
	}

	return map[string]interface{}{
		"primary":   primaryResult,
		"secondary": secondaryResult,
	}, nil
}

func (s *RecordService) GetAllRecords(ctx context.Context, limit, offset int) (map[string]interface{}, error) {
	primaryRecords, err := s.primaryRepo.FindPaginated(ctx, limit, offset)
	if err != nil {
		return nil, fmt.Errorf("primary find failed: %w", err)
	}

	secondaryRecords, err := s.secondaryRepo.FindPaginated(ctx, limit, offset)
	if err != nil {
		return nil, fmt.Errorf("secondary find failed: %w", err)
	}

	return map[string]interface{}{
		"primary":   primaryRecords,
		"secondary": secondaryRecords,
	}, nil
}

func (s *RecordService) Seed(ctx context.Context) (map[string]interface{}, error) {
	start := time.Now()

	primaryInserted, err := s.seedDB(ctx, true)
	if err != nil {
		return nil, fmt.Errorf("primary seed failed: %w", err)
	}

	secondaryInserted, err := s.seedDB(ctx, false)
	if err != nil {
		return nil, fmt.Errorf("secondary seed failed: %w", err)
	}

	return map[string]interface{}{
		"primaryInserted":   primaryInserted,
		"secondaryInserted": secondaryInserted,
		"durationMs":        time.Since(start).Milliseconds(),
	}, nil
}

func (s *RecordService) seedDB(ctx context.Context, primary bool) (int64, error) {
	var total int64
	batches := seedTotal / seedBatchSize

	for i := 0; i < batches; i++ {
		batch := make([]interface{}, seedBatchSize)
		for j := 0; j < seedBatchSize; j++ {
			idx := i*seedBatchSize + j
			tenant := tenants[idx%len(tenants)]
			batch[j] = &model.RecordDocument{
				ID:        primitive.NewObjectID(),
				Tenant:    tenant,
				Name:      fmt.Sprintf("seed-name-%d", idx),
				Value:     fmt.Sprintf("seed-value-%d", idx),
				CreatedAt: time.Now(),
			}
		}

		var inserted int64
		var err error
		if primary {
			inserted, err = s.primaryRepo.InsertMany(ctx, batch)
		} else {
			inserted, err = s.secondaryRepo.InsertMany(ctx, batch)
		}
		if err != nil {
			return total, err
		}
		total += inserted
	}

	return total, nil
}
