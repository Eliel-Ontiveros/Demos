use std::sync::Arc;
use chrono::Utc;
use crate::model::RecordDocument;
use crate::repository::primary::PrimaryRepository;
use crate::repository::secondary::SecondaryRepository;

const VALID_TENANTS: &[&str] = &["tenant1", "tenant2", "tenant3"];
const SEED_TOTAL: usize = 100_000;
const SEED_BATCH_SIZE: usize = 1_000;

pub struct RecordService {
    pub primary_repo: Arc<PrimaryRepository>,
    pub secondary_repo: Arc<SecondaryRepository>,
}

impl RecordService {
    pub fn new(primary_repo: PrimaryRepository, secondary_repo: SecondaryRepository) -> Self {
        RecordService {
            primary_repo: Arc::new(primary_repo),
            secondary_repo: Arc::new(secondary_repo),
        }
    }

    pub fn is_valid_tenant(tenant: &str) -> bool {
        VALID_TENANTS.contains(&tenant)
    }

    pub async fn create_record(
        &self,
        tenant: &str,
        name: String,
        value: String,
    ) -> Result<(RecordDocument, RecordDocument), mongodb::error::Error> {
        let now = Utc::now();
        let primary_record = RecordDocument {
            id: None,
            tenant: tenant.to_string(),
            name: name.clone(),
            value: value.clone(),
            created_at: now,
        };
        let secondary_record = RecordDocument {
            id: None,
            tenant: tenant.to_string(),
            name,
            value,
            created_at: now,
        };

        let (primary_result, secondary_result) = tokio::join!(
            self.primary_repo.insert(primary_record),
            self.secondary_repo.insert(secondary_record)
        );

        Ok((primary_result?, secondary_result?))
    }

    pub async fn get_all_records(
        &self,
        limit: u64,
        offset: u64,
    ) -> Result<(Vec<RecordDocument>, Vec<RecordDocument>), mongodb::error::Error> {
        let (primary_result, secondary_result) = tokio::join!(
            self.primary_repo.find_paginated(limit, offset),
            self.secondary_repo.find_paginated(limit, offset)
        );

        Ok((primary_result?, secondary_result?))
    }

    pub async fn seed(&self) -> Result<(u64, u64, u64), mongodb::error::Error> {
        let start = std::time::Instant::now();
        let tenants = VALID_TENANTS;
        let num_batches = SEED_TOTAL / SEED_BATCH_SIZE;

        let mut primary_total: u64 = 0;
        let mut secondary_total: u64 = 0;

        for batch_idx in 0..num_batches {
            let mut batch: Vec<RecordDocument> = Vec::with_capacity(SEED_BATCH_SIZE);
            for i in 0..SEED_BATCH_SIZE {
                let tenant = tenants[(batch_idx * SEED_BATCH_SIZE + i) % tenants.len()];
                batch.push(RecordDocument {
                    id: None,
                    tenant: tenant.to_string(),
                    name: format!("seed-{}", batch_idx * SEED_BATCH_SIZE + i),
                    value: format!("value-{}", batch_idx * SEED_BATCH_SIZE + i),
                    created_at: Utc::now(),
                });
            }

            let secondary_batch = batch.clone();
            let (primary_result, secondary_result) = tokio::join!(
                self.primary_repo.insert_many(batch),
                self.secondary_repo.insert_many(secondary_batch)
            );

            primary_total += primary_result?;
            secondary_total += secondary_result?;
        }

        let duration_ms = start.elapsed().as_millis() as u64;
        Ok((primary_total, secondary_total, duration_ms))
    }
}
