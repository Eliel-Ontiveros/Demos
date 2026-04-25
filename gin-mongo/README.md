# Gin Mongo Demo

Proyecto **Gin (Go)** para la evaluación comparativa entre **Micronaut**, **Quarkus**, **Spring Boot WebFlux** y **Gin** en los criterios de peso del artefacto, memoria, tiempos de respuesta y complejidad.

## Descripción

Este proyecto implementa una API REST con [Gin](https://github.com/gin-gonic/gin) en Go 1.22 conectada a dos bases de datos MongoDB independientes (`db_primary` y `db_secondary`). Cuenta con soporte multi-tenant mediante el header `X-Tenant-ID`.

Una de las principales ventajas de Go/Gin es el **tamaño del artefacto**: el binario compilado estático pesa típicamente menos de 20 MB y la imagen Docker basada en `scratch` es extremadamente ligera.

## Requisitos

- **Go 1.22+**
- **Docker** (opcional)
- **MongoDB** disponible en `mongodb://localhost:27017`

## Variables de entorno

| Variable             | Default                      | Descripción                  |
|----------------------|------------------------------|------------------------------|
| `MONGO_PRIMARY_URI`  | `mongodb://localhost:27017`  | URI de la BD primaria        |
| `MONGO_PRIMARY_DB`   | `db_primary`                 | Nombre de la BD primaria     |
| `MONGO_SECONDARY_URI`| `mongodb://localhost:27017`  | URI de la BD secundaria      |
| `MONGO_SECONDARY_DB` | `db_secondary`               | Nombre de la BD secundaria   |
| `PORT`               | `8083`                       | Puerto del servidor HTTP     |

## Ejecutar localmente

```bash
cd gin-mongo
go run cmd/main.go
```

O compilando primero:

```bash
cd gin-mongo
go build -o gin-mongo ./cmd/main.go
./gin-mongo
```

## Ejecutar con Docker

```bash
cd gin-mongo
docker build -t gin-mongo-demo .
docker run --rm -p 8083:8083 \
  -e MONGO_PRIMARY_URI=mongodb://host.docker.internal:27017 \
  -e MONGO_SECONDARY_URI=mongodb://host.docker.internal:27017 \
  gin-mongo-demo
```

## Endpoints

### POST /api/records

Inserta un documento en `db_primary` y `db_secondary` bajo el tenant indicado.

```bash
curl -X POST http://localhost:8083/api/records \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name":"demo","value":"123"}'
```

Respuesta:
```json
{
  "primary":   { "id": "...", "tenant": "tenant1", "name": "demo", "value": "123", "createdAt": "..." },
  "secondary": { "id": "...", "tenant": "tenant1", "name": "demo", "value": "123", "createdAt": "..." }
}
```

### GET /api/records

Consulta **todos** los documentos de ambas bases de datos sin filtros.

```bash
curl -X GET http://localhost:8083/api/records \
  -H "X-Tenant-ID: tenant1"
```

Respuesta:
```json
{
  "primary":   [ { "id": "...", "tenant": "tenant1", ... } ],
  "secondary": [ { "id": "...", "tenant": "tenant1", ... } ]
}
```

### POST /api/seed

Inserta **100,000 registros** en `db_primary` y 100,000 en `db_secondary` en lotes de 1,000, distribuidos entre `tenant1`, `tenant2` y `tenant3`.

```bash
curl -X POST http://localhost:8083/api/seed
```

Respuesta:
```json
{
  "primaryInserted":   100000,
  "secondaryInserted": 100000,
  "durationMs":        1234
}
```

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
