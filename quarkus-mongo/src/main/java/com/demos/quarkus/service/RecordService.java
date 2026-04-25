package com.demos.quarkus.service;

import com.demos.quarkus.entity.RecordDocument;
import com.demos.quarkus.repository.PrimaryRecordRepository;
import com.demos.quarkus.repository.SecondaryRecordRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class RecordService {

    private static final Set<String> VALID_TENANTS = Set.of("tenant1", "tenant2", "tenant3");
    private static final String[] TENANTS = {"tenant1", "tenant2", "tenant3"};
    private static final int SEED_TOTAL = 100_000;
    private static final int SEED_BATCH_SIZE = 1_000;

    @Inject
    PrimaryRecordRepository primaryRepo;

    @Inject
    SecondaryRecordRepository secondaryRepo;

    public void validateTenant(String tenant) {
        if (tenant == null || !VALID_TENANTS.contains(tenant)) {
            throw new BadRequestException("Missing or invalid X-Tenant-ID header. Allowed: tenant1, tenant2, tenant3");
        }
    }

    public Map<String, Object> createRecord(String tenant, String name, String value) {
        validateTenant(tenant);
        RecordDocument primaryRecord = new RecordDocument(tenant, name, value);
        RecordDocument secondaryRecord = new RecordDocument(tenant, name, value);

        Map<String, Object> primaryResult = primaryRepo.insert(primaryRecord);
        Map<String, Object> secondaryResult = secondaryRepo.insert(secondaryRecord);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("primary", primaryResult);
        response.put("secondary", secondaryResult);
        return response;
    }

    public Map<String, Object> getAllRecords() {
        List<Map<String, Object>> primaryRecords = primaryRepo.findAll();
        List<Map<String, Object>> secondaryRecords = secondaryRepo.findAll();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("primary", primaryRecords);
        response.put("secondary", secondaryRecords);
        return response;
    }

    public Map<String, Object> seed() {
        long start = System.currentTimeMillis();
        int batches = SEED_TOTAL / SEED_BATCH_SIZE;

        long primaryInserted = 0;
        long secondaryInserted = 0;

        for (int batchIndex = 0; batchIndex < batches; batchIndex++) {
            int startIndex = batchIndex * SEED_BATCH_SIZE;
            List<Document> batch = createSeedBatch(startIndex, SEED_BATCH_SIZE);
            primaryRepo.insertMany(new ArrayList<>(batch));
            secondaryRepo.insertMany(createSeedBatch(startIndex, SEED_BATCH_SIZE));
            primaryInserted += batch.size();
            secondaryInserted += batch.size();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("primaryInserted", primaryInserted);
        response.put("secondaryInserted", secondaryInserted);
        response.put("durationMs", System.currentTimeMillis() - start);
        return response;
    }

    private List<Document> createSeedBatch(int startIndex, int size) {
        List<Document> batch = new ArrayList<>(size);
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < size; i++) {
            int globalIndex = startIndex + i;
            String tenant = TENANTS[globalIndex % TENANTS.length];
            Document doc = new Document();
            doc.append("_id", new ObjectId());
            doc.append("tenant", tenant);
            doc.append("name", "seed-name-" + globalIndex);
            doc.append("value", "seed-value-" + globalIndex);
            doc.append("createdAt", now.toString());
            batch.add(doc);
        }
        return batch;
    }
}
