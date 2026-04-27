# Stage 1: Build
FROM rust:1.88-alpine AS builder

RUN apk add --no-cache musl-dev pkgconf openssl-dev openssl-libs-static

WORKDIR /app

COPY Cargo.toml Cargo.lock* ./

RUN mkdir src && echo "fn main() {}" > src/main.rs && \
    cargo build --release --target x86_64-unknown-linux-musl && \
    rm -f target/x86_64-unknown-linux-musl/release/deps/axum_mongo*

COPY src ./src

RUN cargo build --release --target x86_64-unknown-linux-musl

# Stage 2: Runtime
FROM alpine:3.19

RUN apk add --no-cache ca-certificates

WORKDIR /app

COPY --from=builder /app/target/x86_64-unknown-linux-musl/release/axum-mongo ./axum-mongo

ENV MONGO_PRIMARY_URI=mongodb://localhost:27017
ENV MONGO_PRIMARY_DB=db_primary
ENV MONGO_SECONDARY_URI=mongodb://localhost:27017
ENV MONGO_SECONDARY_DB=db_secondary
ENV PORT=8084

EXPOSE 8084

CMD ["./axum-mongo"]
