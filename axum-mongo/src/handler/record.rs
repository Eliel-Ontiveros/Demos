use axum::{
    extract::State,
    http::{HeaderMap, StatusCode},
    Json,
};
use serde_json::{json, Value};
use std::sync::Arc;

use crate::model::CreateRecordRequest;
use crate::service::RecordService;

fn extract_tenant(headers: &HeaderMap) -> Option<String> {
    headers
        .get("x-tenant-id")
        .and_then(|v| v.to_str().ok())
        .map(|s| s.to_string())
}

pub async fn create(
    State(service): State<Arc<RecordService>>,
    headers: HeaderMap,
    Json(body): Json<CreateRecordRequest>,
) -> Result<Json<Value>, (StatusCode, Json<Value>)> {
    let tenant = match extract_tenant(&headers) {
        Some(t) if RecordService::is_valid_tenant(&t) => t,
        _ => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(json!({
                    "error": "Missing or invalid X-Tenant-ID header. Allowed: tenant1, tenant2, tenant3"
                })),
            ));
        }
    };

    match service.create_record(&tenant, body.name, body.value).await {
        Ok((primary, secondary)) => Ok(Json(json!({
            "primary": primary,
            "secondary": secondary,
        }))),
        Err(e) => Err((
            StatusCode::INTERNAL_SERVER_ERROR,
            Json(json!({ "error": e.to_string() })),
        )),
    }
}

pub async fn list(
    State(service): State<Arc<RecordService>>,
    headers: HeaderMap,
) -> Result<Json<Value>, (StatusCode, Json<Value>)> {
    let _tenant = match extract_tenant(&headers) {
        Some(t) if RecordService::is_valid_tenant(&t) => t,
        _ => {
            return Err((
                StatusCode::BAD_REQUEST,
                Json(json!({
                    "error": "Missing or invalid X-Tenant-ID header. Allowed: tenant1, tenant2, tenant3"
                })),
            ));
        }
    };

    match service.get_all_records().await {
        Ok((primary, secondary)) => Ok(Json(json!({
            "primary": primary,
            "secondary": secondary,
        }))),
        Err(e) => Err((
            StatusCode::INTERNAL_SERVER_ERROR,
            Json(json!({ "error": e.to_string() })),
        )),
    }
}
