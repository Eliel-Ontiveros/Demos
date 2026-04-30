use mongodb::{Database, Collection, bson::doc, options::FindOptions};
use futures::TryStreamExt;
use crate::model::RecordDocument;

pub struct PrimaryRepository {
    collection: Collection<RecordDocument>,
}

impl PrimaryRepository {
    pub fn new(db: &Database) -> Self {
        PrimaryRepository {
            collection: db.collection("records"),
        }
    }

    pub async fn insert(&self, record: RecordDocument) -> Result<RecordDocument, mongodb::error::Error> {
        let result = self.collection.insert_one(record.clone()).await?;
        let mut inserted = record;
        inserted.id = result.inserted_id.as_object_id();
        Ok(inserted)
    }

    pub async fn find_all(&self) -> Result<Vec<RecordDocument>, mongodb::error::Error> {
        let cursor = self.collection.find(doc! {}).await?;
        cursor.try_collect().await
    }

    pub async fn find_paginated(&self, limit: u64, offset: u64) -> Result<Vec<RecordDocument>, mongodb::error::Error> {
        let options = FindOptions::builder()
            .limit(limit.min(i64::MAX as u64) as i64)
            .skip(offset)
            .build();
        let cursor = self.collection.find(doc! {}).with_options(options).await?;
        cursor.try_collect().await
    }

    pub async fn insert_many(&self, records: Vec<RecordDocument>) -> Result<u64, mongodb::error::Error> {
        let result = self.collection.insert_many(records).await?;
        Ok(result.inserted_ids.len() as u64)
    }
}
