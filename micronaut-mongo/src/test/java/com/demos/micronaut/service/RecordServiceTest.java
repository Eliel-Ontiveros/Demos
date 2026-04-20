package com.demos.micronaut.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordServiceTest {

    @Test
    void validatesTenants() {
        assertTrue(RecordService.isValidTenant("tenant1"));
        assertTrue(RecordService.isValidTenant("tenant2"));
        assertTrue(RecordService.isValidTenant("tenant3"));

        assertFalse(RecordService.isValidTenant("tenant4"));
        assertFalse(RecordService.isValidTenant(null));
        assertFalse(RecordService.isValidTenant(""));
    }
}
