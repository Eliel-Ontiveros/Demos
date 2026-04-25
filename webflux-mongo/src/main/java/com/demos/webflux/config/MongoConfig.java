package com.demos.webflux.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.primary.uri}")
    private String primaryUri;

    @Value("${spring.data.mongodb.primary.database}")
    private String primaryDatabase;

    @Value("${spring.data.mongodb.secondary.uri}")
    private String secondaryUri;

    @Value("${spring.data.mongodb.secondary.database}")
    private String secondaryDatabase;

    @Bean(destroyMethod = "close")
    @Primary
    public MongoClient primaryMongoClient() {
        return MongoClients.create(primaryUri);
    }

    @Bean(destroyMethod = "close")
    @Qualifier("secondaryMongoClient")
    public MongoClient secondaryMongoClient() {
        return MongoClients.create(secondaryUri);
    }

    @Bean
    @Primary
    public ReactiveMongoTemplate primaryMongoTemplate(MongoClient primaryMongoClient) {
        return new ReactiveMongoTemplate(primaryMongoClient, primaryDatabase);
    }

    @Bean
    @Qualifier("secondaryMongoTemplate")
    public ReactiveMongoTemplate secondaryMongoTemplate(
            @Qualifier("secondaryMongoClient") MongoClient secondaryMongoClient) {
        return new ReactiveMongoTemplate(secondaryMongoClient, secondaryDatabase);
    }
}
