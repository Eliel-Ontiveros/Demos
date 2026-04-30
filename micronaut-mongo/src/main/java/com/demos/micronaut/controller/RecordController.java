package com.demos.micronaut.controller;

import com.demos.micronaut.dto.CreateRecordRequest;
import com.demos.micronaut.entity.RecordDocument;
import com.demos.micronaut.service.RecordService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @Post
    public Publisher<MutableHttpResponse<Object>> createRecord(@Header("X-Tenant-ID") String tenant,
                                                   @Body @Valid @NotNull CreateRecordRequest request) {
        if (tenant == null || tenant.isBlank()) {
            return Mono.just(HttpResponse.badRequest(Map.of("error", "Missing X-Tenant-ID header")));
        }

        return recordService.createRecord(tenant, request.getName(), request.getValue())
                .map(response -> HttpResponse.ok((Object) response))
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(HttpResponse.badRequest(Map.of("error", ex.getMessage()))))
                .onErrorResume(ex -> Mono.just(HttpResponse.serverError(Map.of("error", "Database error"))));
    }

    @Get
    public Publisher<MutableHttpResponse<Object>> getAllRecords(
            @Header("X-Tenant-ID") String tenant,
            @QueryValue(defaultValue = "100") int limit,
            @QueryValue(defaultValue = "0") int offset) {
        if (tenant == null || tenant.isBlank()) {
            return Mono.just(HttpResponse.badRequest(Map.of("error", "Missing X-Tenant-ID header")));
        }

        int effectiveLimit = Math.min(limit, 1000);
        return recordService.getAllRecords(tenant, effectiveLimit, offset)
                .map((Map<String, List<RecordDocument>> response) -> HttpResponse.ok((Object) response))
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(HttpResponse.badRequest(Map.of("error", ex.getMessage()))))
                .onErrorResume(ex -> Mono.just(HttpResponse.serverError(Map.of("error", "Database error"))));
    }
}
