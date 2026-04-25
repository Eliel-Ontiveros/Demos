# Quarkus Mongo Demo

Proyecto de evaluación comparativa de frameworks. Implementa un servicio multi-tenant con conexión a dos bases de datos MongoDB usando **Quarkus 3.x** con Java 21 y Maven.

## Rol en la comparación

Este proyecto forma parte de una evaluación de:
- **Peso del artefacto** (JAR / imagen Docker)
- **Consumo de memoria** en reposo y bajo carga
- **Tiempos de respuesta** en operaciones de lectura y escritura
- **Complejidad** del código

Los otros proyectos en comparación son:
- `micronaut-mongo/` — Micronaut 4.x
- *(próximamente)* `webflux-mongo/` — Spring WebFlux

---

## Prerequisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java JDK    | 21            |
| Maven       | 3.9+ (o usar `./mvnw`) |
| MongoDB     | Corriendo en `localhost:27017` |
| Docker      | Para construir y correr la imagen |

---

## Correr localmente

### Con MongoDB en Docker

```bash
docker run -d -p 27017:27017 --name mongo-demo mongo:7
```

### Levantar el proyecto

```bash
cd quarkus-mongo
./mvnw quarkus:dev
```

El servidor arrancará en `http://localhost:8081`.

---

## Construir y correr con Docker

```bash
# Construir la imagen
docker build -t quarkus-mongo-demo .

# Correr el contenedor (apuntando al mongo local)
docker run -p 8081:8081 \
  -e QUARKUS_MONGODB_PRIMARY_CONNECTION_STRING=mongodb://host.docker.internal:27017 \
  -e QUARKUS_MONGODB_SECONDARY_CONNECTION_STRING=mongodb://host.docker.internal:27017 \
  quarkus-mongo-demo
```

---

## Endpoints

### POST /api/records

Inserta un documento en `db_primary` y `db_secondary` bajo el tenant indicado.

**Headers:** `X-Tenant-ID: tenant1` (o `tenant2` / `tenant3`)

```bash
curl -X POST http://localhost:8081/api/records \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant1" \
  -d '{"name": "demo", "value": "valor-123"}'
```

**Respuesta:**
```json
{
  "primary": { "id": "...", "tenant": "tenant1", "name": "demo", "value": "valor-123", "createdAt": "..." },
  "secondary": { "id": "...", "tenant": "tenant1", "name": "demo", "value": "valor-123", "createdAt": "..." }
}
```

---

### GET /api/records

Consulta **todos** los documentos de `db_primary` y `db_secondary` sin filtros.

**Headers:** `X-Tenant-ID: tenant1`

```bash
curl -X GET http://localhost:8081/api/records \
  -H "X-Tenant-ID: tenant1"
```

**Respuesta:**
```json
{
  "primary": [...],
  "secondary": [...]
}
```

---

### POST /api/seed

Inserta 100,000 registros en cada base de datos (total 200,000), distribuidos entre 3 tenants en lotes de 1,000.

```bash
curl -X POST http://localhost:8081/api/seed
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

| Métrica                         | Valor |
|---------------------------------|-------|
| Peso JAR (MB)                   | -     |
| Peso imagen Docker              | -     |
| Memoria en reposo               | -     |
| Tiempo arranque                 | -     |
| Tiempo POST /api/records        | -     |
| Tiempo GET /api/records (100k)  | -     |
| Tiempo POST /api/seed           | -     |

---

## Estructura del proyecto

```
quarkus-mongo/
├── src/main/java/com/demos/quarkus/
│   ├── entity/RecordDocument.java
│   ├── repository/PrimaryRecordRepository.java
│   ├── repository/SecondaryRecordRepository.java
│   ├── service/RecordService.java
│   └── resource/
│       ├── RecordResource.java
│       └── SeedResource.java
├── src/main/resources/application.properties
├── pom.xml
├── Dockerfile
└── README.md
```
