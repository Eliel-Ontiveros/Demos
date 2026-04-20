package com.demos.micronaut.entity;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
public class RecordDocument {
    private String id;
    private String tenant;
    private String name;
    private String value;
    private LocalDateTime createdAt;

    public RecordDocument() {
    }

    public RecordDocument(String id, String tenant, String name, String value, LocalDateTime createdAt) {
        this.id = id;
        this.tenant = tenant;
        this.name = name;
        this.value = value;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
