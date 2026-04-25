package com.demos.webflux.controller;

import com.demos.webflux.service.RecordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/seed")
public class SeedController {

    private final RecordService recordService;

    public SeedController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping
    public Mono<Map<String, Object>> seed() {
        return recordService.seed();
    }
}
