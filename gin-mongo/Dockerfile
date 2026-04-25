# Stage 1: Build
FROM golang:1.22-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o gin-mongo ./cmd/main.go

# Stage 2: Runtime
FROM scratch

COPY --from=builder /app/gin-mongo /gin-mongo

EXPOSE 8083

ENTRYPOINT ["/gin-mongo"]
