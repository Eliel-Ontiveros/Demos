package com.demos.quarkus.resource;

import com.demos.quarkus.service.RecordService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/records")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecordResource {

    @Inject
    RecordService recordService;

    @POST
    @Blocking
    public Response createRecord(
            @HeaderParam("X-Tenant-ID") String tenantId,
            Map<String, String> body) {
        if (tenantId == null || tenantId.isBlank()) {
            return Response.status(400)
                    .entity(Map.of("error", "Missing X-Tenant-ID header"))
                    .build();
        }
        try {
            Map<String, Object> result = recordService.createRecord(tenantId, body.get("name"), body.get("value"));
            return Response.ok(result).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", "Internal server error: " + e.getMessage())).build();
        }
    }

    @GET
    @Blocking
    public Response getAllRecords(
            @HeaderParam("X-Tenant-ID") String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Response.status(400)
                    .entity(Map.of("error", "Missing X-Tenant-ID header"))
                    .build();
        }
        try {
            recordService.validateTenant(tenantId);
            Map<String, Object> result = recordService.getAllRecords();
            return Response.ok(result).build();
        } catch (BadRequestException e) {
            return Response.status(400).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", "Internal server error: " + e.getMessage())).build();
        }
    }
}
