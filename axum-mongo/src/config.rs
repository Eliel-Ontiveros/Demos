use std::env;

pub struct Config {
    pub mongo_primary_uri: String,
    pub mongo_primary_db: String,
    pub mongo_secondary_uri: String,
    pub mongo_secondary_db: String,
    pub port: u16,
}

impl Config {
    pub fn from_env() -> Self {
        Config {
            mongo_primary_uri: env::var("MONGO_PRIMARY_URI")
                .unwrap_or_else(|_| "mongodb://localhost:27017".to_string()),
            mongo_primary_db: env::var("MONGO_PRIMARY_DB")
                .unwrap_or_else(|_| "db_primary".to_string()),
            mongo_secondary_uri: env::var("MONGO_SECONDARY_URI")
                .unwrap_or_else(|_| "mongodb://localhost:27017".to_string()),
            mongo_secondary_db: env::var("MONGO_SECONDARY_DB")
                .unwrap_or_else(|_| "db_secondary".to_string()),
            port: env::var("PORT")
                .unwrap_or_else(|_| "8084".to_string())
                .parse()
                .unwrap_or(8084),
        }
    }
}
