# Micronaut Mongo Demo

Proyecto Micronaut para la evaluación comparativa entre **Micronaut**, **Quarkus** y **Spring Boot WebFlux** en los criterios de peso, memoria, tiempos de respuesta y complejidad.

## Requisitos previos

- Java 21
- Docker
- MongoDB disponible en `mongodb://localhost:27017`

## Ejecutar localmente

```bash
cd micronaut-mongo
./gradlew run
```

## Ejecutar con Docker

```bash
cd micronaut-mongo
docker build -t micronaut-mongo-demo .
docker run --rm -p 8080:8080 micronaut-mongo-demo
```

## Endpoints

### POST /api/records

Inserta un documento en `db_primary` y `db_secondary`.

```bash
curl -X POST http://localhost:8080/api/records \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name":"demo","value":"123"}'
```

### GET /api/records

Consulta todos los documentos de ambas bases.

```bash
curl -X GET http://localhost:8080/api/records \
  -H "X-Tenant-ID: tenant1"
```

### POST /api/seed

Inserta 100,000 registros en `db_primary` y 100,000 en `db_secondary` en lotes de 1,000.

```bash
curl -X POST http://localhost:8080/api/seed
```

## Métricas de evaluación

| Métrica             | Valor |
|---------------------|-------|
| Peso JAR (MB)       | -     |
| Peso imagen Docker  | -     |
| Memoria en reposo   | -     |
| Tiempo arranque     | -     |
| Tiempo POST /api/records | - |
| Tiempo GET /api/records (100k) | - |
| Tiempo POST /api/seed  | -  |
