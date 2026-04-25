use mongodb::{Client, Database, options::ClientOptions};
use crate::config::Config;

pub struct DbClients {
    pub primary_db: Database,
    pub secondary_db: Database,
}

pub async fn init_db(config: &Config) -> Result<DbClients, mongodb::error::Error> {
    let primary_options = ClientOptions::parse(&config.mongo_primary_uri).await?;
    let primary_client = Client::with_options(primary_options)?;
    let primary_db = primary_client.database(&config.mongo_primary_db);

    let secondary_options = ClientOptions::parse(&config.mongo_secondary_uri).await?;
    let secondary_client = Client::with_options(secondary_options)?;
    let secondary_db = secondary_client.database(&config.mongo_secondary_db);

    Ok(DbClients {
        primary_db,
        secondary_db,
    })
}
