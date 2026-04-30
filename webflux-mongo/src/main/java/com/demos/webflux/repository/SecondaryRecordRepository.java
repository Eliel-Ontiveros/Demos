package com.demos.webflux.repository;

import com.demos.webflux.entity.RecordDocument;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Repository
public class SecondaryRecordRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public SecondaryRecordRepository(@Qualifier("secondaryMongoTemplate") ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<RecordDocument> insert(RecordDocument record) {
        return mongoTemplate.insert(record);
    }

    public Mono<List<RecordDocument>> insertAll(Collection<RecordDocument> records) {
        return mongoTemplate.insertAll(records).collectList();
    }

    public Flux<RecordDocument> findAll() {
        return mongoTemplate.find(new Query(), RecordDocument.class);
    }

    public Flux<RecordDocument> findPaginated(int limit, int offset) {
        Query query = new Query().skip(offset).limit(limit);
        return mongoTemplate.find(query, RecordDocument.class);
    }
}
