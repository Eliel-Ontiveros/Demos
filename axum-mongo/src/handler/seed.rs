use axum::{
    extract::State,
    http::StatusCode,
    Json,
};
use serde_json::{json, Value};
use std::sync::Arc;

use crate::service::RecordService;

pub async fn seed(
    State(service): State<Arc<RecordService>>,
) -> Result<Json<Value>, (StatusCode, Json<Value>)> {
    match service.seed().await {
        Ok((primary_inserted, secondary_inserted, duration_ms)) => Ok(Json(json!({
            "primaryInserted": primary_inserted,
            "secondaryInserted": secondary_inserted,
            "durationMs": duration_ms,
        }))),
        Err(e) => Err((
            StatusCode::INTERNAL_SERVER_ERROR,
            Json(json!({ "error": e.to_string() })),
        )),
    }
}
