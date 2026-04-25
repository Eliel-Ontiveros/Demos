mod config;
mod db;
mod handler;
mod model;
mod repository;
mod service;

use std::sync::Arc;
use axum::{routing::{get, post}, Router};
use tracing_subscriber::{EnvFilter, fmt};

use config::Config;
use db::init_db;
use repository::primary::PrimaryRepository;
use repository::secondary::SecondaryRepository;
use service::RecordService;

#[derive(Clone)]
pub struct AppState {
    pub primary_db: mongodb::Database,
    pub secondary_db: mongodb::Database,
}

#[tokio::main]
async fn main() {
    let _ = dotenvy::dotenv();

    fmt()
        .with_env_filter(
            EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| EnvFilter::new("axum_mongo=info,info")),
        )
        .init();

    let config = Config::from_env();
    let port = config.port;

    let db_clients = init_db(&config).await.expect("Failed to connect to MongoDB");

    let primary_repo = PrimaryRepository::new(&db_clients.primary_db);
    let secondary_repo = SecondaryRepository::new(&db_clients.secondary_db);
    let service = Arc::new(RecordService::new(primary_repo, secondary_repo));

    let app = Router::new()
        .route("/api/records", post(handler::record::create))
        .route("/api/records", get(handler::record::list))
        .route("/api/seed", post(handler::seed::seed))
        .with_state(service);

    let listener = tokio::net::TcpListener::bind(format!("0.0.0.0:{}", port))
        .await
        .expect("Failed to bind");

    tracing::info!("Axum server listening on port {}", port);
    axum::serve(listener, app).await.expect("Server error");
}
