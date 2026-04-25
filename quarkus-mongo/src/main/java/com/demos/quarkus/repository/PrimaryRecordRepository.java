package com.demos.quarkus.repository;

import com.demos.quarkus.entity.RecordDocument;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.mongodb.MongoClientName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PrimaryRecordRepository {

    @Inject
    @MongoClientName("primary")
    MongoClient mongoClient;

    @ConfigProperty(name = "quarkus.mongodb.primary.database", defaultValue = "db_primary")
    String database;
    private static final String COLLECTION = "records";

    private MongoCollection<Document> getCollection() {
        return mongoClient.getDatabase(database).getCollection(COLLECTION);
    }

    public Map<String, Object> insert(RecordDocument record) {
        Document doc = toDocument(record);
        getCollection().insertOne(doc);
        return documentToMap(doc);
    }

    public void insertMany(List<Document> documents) {
        getCollection().insertMany(documents);
    }

    public List<Map<String, Object>> findAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Document doc : getCollection().find()) {
            result.add(documentToMap(doc));
        }
        return result;
    }

    private Document toDocument(RecordDocument record) {
        Document doc = new Document();
        if (record.id != null) {
            doc.append("_id", record.id);
        }
        doc.append("tenant", record.tenant);
        doc.append("name", record.name);
        doc.append("value", record.value);
        doc.append("createdAt", record.createdAt != null ? record.createdAt.toString() : null);
        return doc;
    }

    private Map<String, Object> documentToMap(Document doc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", doc.getObjectId("_id") != null ? doc.getObjectId("_id").toHexString() : null);
        map.put("tenant", doc.getString("tenant"));
        map.put("name", doc.getString("name"));
        map.put("value", doc.getString("value"));
        map.put("createdAt", doc.getString("createdAt"));
        return map;
    }
}
