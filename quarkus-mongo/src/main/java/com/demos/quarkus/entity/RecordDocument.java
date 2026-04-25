package com.demos.quarkus.entity;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecordDocument {

    public ObjectId id;
    public String tenant;
    public String name;
    public String value;
    public LocalDateTime createdAt;

    public RecordDocument() {}

    public RecordDocument(String tenant, String name, String value) {
        this.id = new ObjectId();
        this.tenant = tenant;
        this.name = name;
        this.value = value;
        this.createdAt = LocalDateTime.now();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id != null ? id.toHexString() : null);
        map.put("tenant", tenant);
        map.put("name", name);
        map.put("value", value);
        map.put("createdAt", createdAt != null ? createdAt.toString() : null);
        return map;
    }
}
