package com.demos.micronaut.repository;

import com.demos.micronaut.entity.RecordDocument;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Singleton
public class PrimaryRecordRepository {

    private static final String COLLECTION = "records";
    private final MongoDatabase database;

    public PrimaryRecordRepository(@Named("primary") MongoDatabase database) {
        this.database = database;
    }

    public Mono<RecordDocument> insert(RecordDocument record) {
        return Mono.from(getCollection().insertOne(toDocument(record)))
                .thenReturn(record);
    }

    public Flux<RecordDocument> findAll() {
        return Flux.from(getCollection().find())
                .map(this::fromDocument);
    }

    public Flux<RecordDocument> findPaginated(int limit, int offset) {
        return Flux.from(getCollection().find().skip(offset).limit(limit))
                .map(this::fromDocument);
    }

    public Mono<Long> insertMany(List<RecordDocument> records) {
        List<Document> docs = records.stream().map(this::toDocument).toList();
        return Mono.from(getCollection().insertMany(docs)).thenReturn((long) records.size());
    }

    private MongoCollection<Document> getCollection() {
        return database.getCollection(COLLECTION);
    }

    private Document toDocument(RecordDocument record) {
        return new Document("_id", new ObjectId(record.getId()))
                .append("tenant", record.getTenant())
                .append("name", record.getName())
                .append("value", record.getValue())
                .append("createdAt", Date.from(record.getCreatedAt().toInstant(ZoneOffset.UTC)));
    }

    private RecordDocument fromDocument(Document doc) {
        Date createdAtDate = doc.getDate("createdAt");
        LocalDateTime createdAt = createdAtDate == null
                ? null
                : LocalDateTime.ofInstant(createdAtDate.toInstant(), ZoneOffset.UTC);

        ObjectId id = doc.getObjectId("_id");
        return new RecordDocument(
                id == null ? null : id.toHexString(),
                doc.getString("tenant"),
                doc.getString("name"),
                doc.getString("value"),
                createdAt
        );
    }
}
