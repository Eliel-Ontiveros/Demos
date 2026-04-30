package com.demos.micronaut.service;

import com.demos.micronaut.entity.RecordDocument;
import com.demos.micronaut.repository.PrimaryRecordRepository;
import com.demos.micronaut.repository.SecondaryRecordRepository;
import jakarta.inject.Singleton;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class RecordService {

    private static final Set<String> VALID_TENANTS = Set.of("tenant1", "tenant2", "tenant3");
    private static final int SEED_TOTAL = 100_000;
    private static final int SEED_BATCH_SIZE = 1_000;

    private final PrimaryRecordRepository primaryRecordRepository;
    private final SecondaryRecordRepository secondaryRecordRepository;

    public RecordService(PrimaryRecordRepository primaryRecordRepository,
                         SecondaryRecordRepository secondaryRecordRepository) {
        this.primaryRecordRepository = primaryRecordRepository;
        this.secondaryRecordRepository = secondaryRecordRepository;
    }

    public Mono<Map<String, RecordDocument>> createRecord(String tenant, String name, String value) {
        validateTenant(tenant);
        LocalDateTime now = LocalDateTime.now();

        RecordDocument primary = new RecordDocument(new ObjectId().toHexString(), tenant, name, value, now);
        RecordDocument secondary = new RecordDocument(new ObjectId().toHexString(), tenant, name, value, now);

        return Mono.zip(
                primaryRecordRepository.insert(primary),
                secondaryRecordRepository.insert(secondary)
        ).map(tuple -> Map.of("primary", tuple.getT1(), "secondary", tuple.getT2()));
    }

    public Mono<Map<String, List<RecordDocument>>> getAllRecords(String tenant) {
        return getAllRecords(tenant, 100, 0);
    }

    public Mono<Map<String, List<RecordDocument>>> getAllRecords(String tenant, int limit, int offset) {
        validateTenant(tenant);
        return Mono.zip(
                primaryRecordRepository.findPaginated(limit, offset).collectList(),
                secondaryRecordRepository.findPaginated(limit, offset).collectList()
        ).map(tuple -> Map.of("primary", tuple.getT1(), "secondary", tuple.getT2()));
    }

    public Mono<Map<String, Object>> seed() {
        long start = System.currentTimeMillis();

        Mono<Long> primaryInserted = seedRepository(true);
        Mono<Long> secondaryInserted = seedRepository(false);

        return Mono.zip(primaryInserted, secondaryInserted)
                .map(tuple -> Map.<String, Object>of(
                        "primaryInserted", tuple.getT1(),
                        "secondaryInserted", tuple.getT2(),
                        "durationMs", System.currentTimeMillis() - start
                ));
    }

    public static boolean isValidTenant(String tenant) {
        return tenant != null && VALID_TENANTS.contains(tenant);
    }

    private void validateTenant(String tenant) {
        if (!isValidTenant(tenant)) {
            throw new IllegalArgumentException("Invalid tenant. Allowed values: tenant1, tenant2, tenant3");
        }
    }

    private Mono<Long> seedRepository(boolean primary) {
        int batches = SEED_TOTAL / SEED_BATCH_SIZE;
        return Flux.range(0, batches)
                .concatMap(batchIndex -> {
                    int startIndex = batchIndex * SEED_BATCH_SIZE;
                    List<RecordDocument> batch = createSeedBatch(startIndex, SEED_BATCH_SIZE);
                    return primary
                            ? primaryRecordRepository.insertMany(batch)
                            : secondaryRecordRepository.insertMany(batch);
                })
                .reduce(0L, Long::sum);
    }

    private List<RecordDocument> createSeedBatch(int startIndex, int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        List<RecordDocument> records = new ArrayList<>(batchSize);
        for (int index = startIndex; index < startIndex + batchSize; index++) {
            String tenant = switch (index % 3) {
                case 0 -> "tenant1";
                case 1 -> "tenant2";
                default -> "tenant3";
            };
            records.add(new RecordDocument(
                    new ObjectId().toHexString(),
                    tenant,
                    "seed-name-" + index,
                    "seed-value-" + index,
                    now
            ));
        }
        return records;
    }
}
