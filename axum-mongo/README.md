# Axum Mongo Demo

Proyecto de evaluación comparativa de frameworks. Implementa un servicio multi-tenant con conexión a dos bases de datos MongoDB usando **Axum 0.7** con Rust (edición 2021).

## Rol en la comparación

Este proyecto forma parte de una evaluación de:
- **Peso del artefacto** (binario / imagen Docker)
- **Consumo de memoria** en reposo y bajo carga
- **Tiempos de respuesta** en operaciones de lectura y escritura
- **Complejidad** del código

Los otros proyectos en comparación son:
- `micronaut-mongo/` — Micronaut 4.x (Java, Gradle) — Puerto 8080
- `quarkus-mongo/` — Quarkus 3.x (Java, Maven) — Puerto 8081
- `webflux-mongo/` — Spring Boot WebFlux (Java, Maven) — Puerto 8082
- `gin-mongo/` — Gin (Go) — Puerto 8083
- **`axum-mongo/` — Axum (Rust) — Puerto 8084** ← este proyecto

---

## Prerequisitos

| Herramienta  | Versión mínima |
|--------------|----------------|
| Rust         | stable (1.75+) |
| MongoDB      | Corriendo en `localhost:27017` |
| Docker       | Para construir y correr la imagen |

---

## Variables de entorno

| Variable              | Default                     |
|-----------------------|-----------------------------|
| `MONGO_PRIMARY_URI`   | `mongodb://localhost:27017` |
| `MONGO_PRIMARY_DB`    | `db_primary`                |
| `MONGO_SECONDARY_URI` | `mongodb://localhost:27017` |
| `MONGO_SECONDARY_DB`  | `db_secondary`              |
| `PORT`                | `8084`                      |

---

## Correr localmente

### Con MongoDB en Docker

```bash
docker run -d -p 27017:27017 --name mongo-demo mongo:7
```

### Levantar el proyecto

```bash
cd axum-mongo
cargo run
```

El servidor arrancará en `http://localhost:8084`.

---

## Construir y correr con Docker

```bash
cd axum-mongo
docker build -t axum-mongo .
docker run -p 8084:8084 \
  -e MONGO_PRIMARY_URI=mongodb://host.docker.internal:27017 \
  -e MONGO_SECONDARY_URI=mongodb://host.docker.internal:27017 \
  axum-mongo
```

---

## Endpoints

### `POST /api/records` — Crear un registro en ambas DBs

```bash
curl -X POST http://localhost:8084/api/records \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name": "demo", "value": "valor-123"}'
```

**Respuesta:**
```json
{
  "primary": { "_id": "...", "tenant": "tenant1", "name": "demo", "value": "valor-123", "created_at": "..." },
  "secondary": { "_id": "...", "tenant": "tenant1", "name": "demo", "value": "valor-123", "created_at": "..." }
}
```

> Retorna **400** si falta el header `X-Tenant-ID` o si el tenant no es válido.

---

### `GET /api/records` — Consultar todos los registros de ambas DBs

```bash
curl -X GET http://localhost:8084/api/records \
  -H "X-Tenant-ID: tenant1"
```

**Respuesta:**
```json
{
  "primary": [ { ... }, { ... } ],
  "secondary": [ { ... }, { ... } ]
}
```

> Retorna **400** si falta el header `X-Tenant-ID` o si el tenant no es válido.

---

### `POST /api/seed` — Poblar con 100,000 registros en cada DB

```bash
curl -X POST http://localhost:8084/api/seed
```

**Respuesta:**
```json
{
  "primaryInserted": 100000,
  "secondaryInserted": 100000,
  "durationMs": 12345
}
```

---

## Métricas de evaluación

| Métrica                        | Valor |
|-------------------------------|-------|
| Peso binario (MB)             | -     |
| Peso imagen Docker            | -     |
| Memoria en reposo             | -     |
| Tiempo arranque               | -     |
| Tiempo POST /api/records      | -     |
| Tiempo GET /api/records (100k)| -     |
| Tiempo POST /api/seed         | -     |
