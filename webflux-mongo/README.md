# Spring Boot WebFlux Mongo Demo

Proyecto **Spring Boot WebFlux** para la evaluación comparativa entre **Micronaut**, **Quarkus** y **Spring Boot WebFlux** en los criterios de peso del artefacto, memoria, tiempos de respuesta y complejidad.

## Descripción

Aplicación reactiva que expone una API REST para:
- Insertar registros en **dos bases de datos MongoDB** simultáneamente (`db_primary` y `db_secondary`)
- Consultar todos los registros de ambas bases de datos
- Poblar ambas bases de datos con **100,000 registros** de seed para pruebas de carga

La resolución de **tenant** se realiza mediante el header HTTP `X-Tenant-ID` (valores válidos: `tenant1`, `tenant2`, `tenant3`).

## Requisitos previos

| Herramienta  | Versión mínima |
|--------------|----------------|
| Java JDK     | 21             |
| Maven        | 3.9 (o usar `./mvnw`) |
| MongoDB      | corriendo en `localhost:27017` |
| Docker       | para build/run con contenedor |

## Ejecutar localmente

```bash
# Levantar MongoDB (si no lo tienes corriendo)
docker run -d -p 27017:27017 --name mongo-demo mongo:7

# Desde el directorio webflux-mongo/
./mvnw spring-boot:run
```

El servidor arranca en el puerto **8082**.

## Ejecutar con Docker

```bash
# Build de la imagen
docker build -t webflux-mongo-demo .

# Ejecutar el contenedor (asegurarse de que MongoDB esté accesible)
docker run -p 8082:8082 \
  -e SPRING_DATA_MONGODB_PRIMARY_URI=mongodb://host.docker.internal:27017 \
  -e SPRING_DATA_MONGODB_SECONDARY_URI=mongodb://host.docker.internal:27017 \
  webflux-mongo-demo
```

## Endpoints

### `POST /api/records` — Insertar registro en ambas DBs

```bash
curl -X POST http://localhost:8082/api/records \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name": "Mi Registro", "value": "Valor de prueba"}'
```

Respuesta:
```json
{
  "primary": { "id": "...", "tenant": "tenant1", "name": "Mi Registro", "value": "Valor de prueba", "createdAt": "..." },
  "secondary": { "id": "...", "tenant": "tenant1", "name": "Mi Registro", "value": "Valor de prueba", "createdAt": "..." }
}
```

> Retorna **400** si falta el header `X-Tenant-ID` o si el tenant no es válido.

### `GET /api/records` — Consultar todos los registros de ambas DBs

```bash
curl -X GET http://localhost:8082/api/records \
  -H "X-Tenant-ID: tenant1"
```

Respuesta:
```json
{
  "primary": [ { ... }, { ... } ],
  "secondary": [ { ... }, { ... } ]
}
```

> Retorna **400** si falta el header `X-Tenant-ID` o si el tenant no es válido.

### `POST /api/seed` — Poblar con 100,000 registros en cada DB

```bash
curl -X POST http://localhost:8082/api/seed
```

Respuesta:
```json
{
  "primaryInserted": 100000,
  "secondaryInserted": 100000,
  "durationMs": 12345
}
```

## Métricas de evaluación

| Métrica                         | Valor |
|---------------------------------|-------|
| Peso JAR (MB)                   | -     |
| Peso imagen Docker              | -     |
| Memoria en reposo               | -     |
| Tiempo arranque                 | -     |
| Tiempo POST /api/records        | -     |
| Tiempo GET /api/records (100k)  | -     |
| Tiempo POST /api/seed           | -     |
