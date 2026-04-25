package com.demos.quarkus.resource;

import com.demos.quarkus.service.RecordService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/seed")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SeedResource {

    @Inject
    RecordService recordService;

    @POST
    @Blocking
    public Response seed() {
        try {
            Map<String, Object> result = recordService.seed();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(500).entity(Map.of("error", "Seed failed: " + e.getMessage())).build();
        }
    }
}
