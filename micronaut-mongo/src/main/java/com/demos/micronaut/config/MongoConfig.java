package com.demos.micronaut.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
public class MongoConfig {

    private final MongoDbSettings primary;
    private final MongoDbSettings secondary;

    public MongoConfig(PrimaryMongoDbSettings primary, SecondaryMongoDbSettings secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Singleton
    @Bean(preDestroy = "close")
    @Named("primary")
    public MongoClient primaryMongoClient() {
        return MongoClients.create(primary.getUri());
    }

    @Singleton
    @Bean(preDestroy = "close")
    @Named("secondary")
    public MongoClient secondaryMongoClient() {
        return MongoClients.create(secondary.getUri());
    }

    @Singleton
    @Named("primary")
    public MongoDatabase primaryDatabase(@Named("primary") MongoClient client) {
        return client.getDatabase(primary.getDatabase());
    }

    @Singleton
    @Named("secondary")
    public MongoDatabase secondaryDatabase(@Named("secondary") MongoClient client) {
        return client.getDatabase(secondary.getDatabase());
    }

    @ConfigurationProperties("mongodb.primary")
    @Named("primary")
    public static class PrimaryMongoDbSettings extends MongoDbSettings {
    }

    @ConfigurationProperties("mongodb.secondary")
    @Named("secondary")
    public static class SecondaryMongoDbSettings extends MongoDbSettings {
    }

    public static class MongoDbSettings {
        private String uri;
        private String database;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }
    }
}
