# ── Stage 1: Native image build ──────────────────────────────────
FROM ghcr.io/graalvm/native-image-community:21-ol9 AS build
WORKDIR /home/app
# findutils is required by the Gradle wrapper
RUN microdnf install -y findutils
COPY . .
RUN ./gradlew --no-daemon --no-configuration-cache nativeCompile

# ── Stage 2: Minimal runtime ─────────────────────────────────────
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=build /home/app/build/native/nativeCompile/micronaut-mongo app
EXPOSE 8080
ENTRYPOINT ["/app/app"]
