# syntax=docker/dockerfile:1

# ── Stage 1: Native image build ──────────────────────────────────
FROM ghcr.io/graalvm/native-image-community:21-ol9 AS build
WORKDIR /app
# Maven and findutils (required for classpath scanning)
RUN microdnf install -y maven findutils
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn -Pnative native:compile -DskipTests -q

# ── Stage 2: Minimal runtime ─────────────────────────────────────
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=build /app/target/webflux-mongo app
EXPOSE 8082
ENTRYPOINT ["/app/app"]
