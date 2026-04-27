package config

import "os"

type Config struct {
	MongoPrimaryURI string
	MongoPrimaryDB  string
	MongoSecondaryURI string
	MongoSecondaryDB  string
	Port            string
}

func Load() *Config {
	return &Config{
		MongoPrimaryURI:   getEnv("MONGO_PRIMARY_URI", "mongodb://localhost:27017"),
		MongoPrimaryDB:    getEnv("MONGO_PRIMARY_DB", "db_primary"),
		MongoSecondaryURI: getEnv("MONGO_SECONDARY_URI", "mongodb://localhost:27017"),
		MongoSecondaryDB:  getEnv("MONGO_SECONDARY_DB", "db_secondary"),
		Port:              getEnv("PORT", "8083"),
	}
}

func getEnv(key, defaultValue string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return defaultValue
}
