package com.demos.webflux.controller;

import com.demos.webflux.entity.RecordDocument;
import com.demos.webflux.service.RecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, RecordDocument>>> createRecord(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody Map<String, String> body) {

        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().<Map<String, RecordDocument>>build());
        }

        String name = body.getOrDefault("name", "");
        String value = body.getOrDefault("value", "");

        return recordService.createRecord(tenantId, name, value)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().<Map<String, RecordDocument>>build()));
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, List<RecordDocument>>>> getAllRecords(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {

        if (tenantId == null || tenantId.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().<Map<String, List<RecordDocument>>>build());
        }

        return recordService.getAllRecords(tenantId)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().<Map<String, List<RecordDocument>>>build()));
    }
}
