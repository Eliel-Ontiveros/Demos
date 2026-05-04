# ── Stage 1: Native image build ──────────────────────────────────
FROM ghcr.io/graalvm/native-image-community:21-ol9 AS build
WORKDIR /app
# Maven y findutils requeridos para el classpath scanning de Quarkus
RUN microdnf install -y maven findutils
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -Dnative -DskipTests -q

# ── Stage 2: Minimal runtime ─────────────────────────────────────
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=build /app/target/quarkus-mongo-1.0.0-SNAPSHOT-runner app
EXPOSE 8081
ENTRYPOINT ["/app/app"]
