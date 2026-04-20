package com.demos.micronaut.controller;

import com.demos.micronaut.service.RecordService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller("/api/seed")
public class SeedController {

    private final RecordService recordService;

    public SeedController(RecordService recordService) {
        this.recordService = recordService;
    }

    @Post
    public Publisher<MutableHttpResponse<Object>> seed() {
        return recordService.seed()
                .map(response -> HttpResponse.ok((Object) response))
                .onErrorResume(ex -> Mono.just(HttpResponse.serverError(Map.of("error", "Database error"))));
    }
}
