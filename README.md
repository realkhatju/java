# Product API

A complete Spring Boot CRUD REST API for a product catalog, with a real database, validation, pagination, filtering, consistent error responses, integration tests, and a ready-to-run Postman collection.

## Tech stack

- Java 21
- Spring Boot 4.0.8 (Spring MVC, Spring Data JPA, Hibernate 7, Jakarta Validation)
- H2 file database by default (zero setup)
- PostgreSQL and MySQL profiles
- Maven Wrapper (`./mvnw`)
- JUnit 5 + MockMvc integration tests
- Postman Collection v2.1

## Quick start

Requirements: Java 21+ only. Maven is downloaded automatically by the wrapper.

```bash
cd product-api

# macOS/Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080` and creates `./data/productdb.mv.db` on first run. Ten sample products are seeded automatically.

Check it is running:

```bash
curl http://localhost:8080/actuator/health
```

```json
{"status":"UP"}
```

### Java setup note

If `java -version` fails, install a JDK 21 and point `JAVA_HOME` at it. On macOS with Homebrew:

```bash
brew install openjdk@21
export JAVA_HOME="$(brew --prefix openjdk@21)"
export PATH="$JAVA_HOME/bin:$PATH"
```

## API endpoints

Base path: `/api/v1/products`

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `GET` | `/api/v1/products` | List, search, filter, sort and paginate products | `200` |
| `GET` | `/api/v1/products/{id}` | Get a product by id | `200` |
| `GET` | `/api/v1/products/sku/{sku}` | Get a product by SKU (case-insensitive) | `200` |
| `POST` | `/api/v1/products` | Create a product | `201` + `Location` header |
| `PUT` | `/api/v1/products/{id}` | Replace all product fields | `200` |
| `PATCH` | `/api/v1/products/{id}` | Partially update a product | `200` |
| `DELETE` | `/api/v1/products/{id}` | Delete a product | `204` |
| `GET` | `/actuator/health` | Health check | `200` |

### List query parameters

| Parameter | Default | Description |
| --- | --- | --- |
| `page` | `0` | Zero-based page index |
| `size` | `20` | Page size, 1-100 |
| `sortBy` | `createdAt` | `id`, `sku`, `name`, `category`, `price`, `stock`, `active`, `createdAt`, `updatedAt` |
| `direction` | `desc` | `asc` or `desc` |
| `q` | - | Search in name, SKU and category |
| `category` | - | Exact category filter (case-insensitive) |
| `active` | - | `true` or `false` |
| `minPrice` | - | Minimum price (inclusive) |
| `maxPrice` | - | Maximum price (inclusive) |

Example:

```bash
curl "http://localhost:8080/api/v1/products?q=keyboard&category=Electronics&minPrice=50&sortBy=price&direction=asc&page=0&size=10"
```

### Create

```bash
curl -i -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ELEC-9999",
    "name": "USB Microphone",
    "description": "Cardioid condenser microphone",
    "category": "Electronics",
    "price": 74.99,
    "stock": 15,
    "active": true
  }'
```

`active` is optional on create and defaults to `true`.

### Update (PUT)

All fields except `description` are required:

```bash
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "ELEC-1001",
    "name": "Wireless Mouse Pro",
    "description": "Updated description",
    "category": "Electronics",
    "price": 34.99,
    "stock": 80,
    "active": true
  }'
```

### Patch (PATCH)

Send only the fields you want to change:

```bash
curl -X PATCH http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"price": 31.50, "stock": 60}'
```

### Delete

```bash
curl -X DELETE http://localhost:8080/api/v1/products/1
```

### Error responses

Errors use RFC 7807 `application/problem+json`:

```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more request fields are invalid",
  "instance": "/api/v1/products",
  "timestamp": "2026-10-21T03:47:51.487928Z",
  "errors": {
    "sku": "SKU may contain only letters, numbers, hyphens and underscores",
    "name": "Name is required"
  }
}
```

| Status | When |
| --- | --- |
| `400` | Validation failure, malformed JSON, invalid query parameter |
| `404` | Product not found |
| `409` | Duplicate SKU or database constraint violation |
| `500` | Unexpected server error |

## Postman collection

Files:

- `postman/Product-API.postman_collection.json`
- `postman/Product-API-Local.postman_environment.json`

Import both into Postman, select the **Product API - Local** environment, then run the collection with the Collection Runner.

The collection covers:

- Health check
- Create, read by id, read by SKU, list with filters/sorting/pagination
- Full update with PUT
- Partial update with PATCH
- Delete
- Error cases: 404, duplicate SKU 409, validation 400, invalid sort field, invalid page size

The **Create Product** request generates a unique SKU per run and saves `productId` and `productSku` as collection variables for later requests. Run the requests in order.

Run it from the CLI with Newman:

```bash
npx newman run postman/Product-API.postman_collection.json \
  -e postman/Product-API-Local.postman_environment.json
```

## Database

### Default: H2 file database

No installation required. Data survives restarts in `./data/productdb.mv.db`.

- JDBC URL: `jdbc:h2:file:./data/productdb;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: empty
- Console: `http://localhost:8080/h2-console`

To reset the database, stop the app and delete the `data/` directory.

### PostgreSQL

```bash
docker compose --profile postgres up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Defaults: database `productdb`, user `product`, password `product`, port `5432`.

### MySQL

```bash
docker compose --profile mysql up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Defaults: database `productdb`, user `product`, password `product`, port `3306`.

Override any connection setting with environment variables:

```bash
DB_URL=jdbc:postgresql://db-host:5432/productdb \
DB_USERNAME=myuser \
DB_PASSWORD=mypassword \
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Other settings:

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port |
| `APP_SEED_ENABLED` | `true` | Seed sample products when the table is empty |
| `APP_CORS_ALLOWED_ORIGINS` | `*` | Comma-separated allowed origins for `/api/**` |

## Tests

```bash
./mvnw test
```

The suite contains 17 tests: a context-load test and full-stack MockMvc integration tests for every endpoint, validation, duplicate SKU handling, filtering, sorting, pagination, malformed JSON, 404 responses, unknown endpoints and unsupported methods. Tests run against an in-memory H2 database and never touch the file database.

## Project structure

```text
product-api/
├── pom.xml
├── mvnw / mvnw.cmd
├── docker-compose.yml
├── .env.example
├── postman/
│   ├── Product-API.postman_collection.json
│   └── Product-API-Local.postman_environment.json
└── src/
    ├── main/
    │   ├── java/com/example/productapi/
    │   │   ├── ProductApiApplication.java
    │   │   ├── common/PageResponse.java
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java
    │   │   │   └── DataSeeder.java
    │   │   ├── exception/
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   └── product/
    │   │       ├── Product.java
    │   │       ├── ProductController.java
    │   │       ├── ProductMapper.java
    │   │       ├── ProductRepository.java
    │   │       ├── ProductService.java
    │   │       └── dto/
    │   │           ├── CreateProductRequest.java
    │   │           ├── PatchProductRequest.java
    │   │           ├── ProductResponse.java
    │   │           └── UpdateProductRequest.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-mysql.yml
    │       └── application-postgres.yml
    └── test/
        ├── java/com/example/productapi/
        │   ├── ProductApiApplicationTests.java
        │   └── ProductControllerIntegrationTest.java
        └── resources/application-test.yml
```

## Design notes

- Layered structure: controller → service → repository. DTOs are separate from the JPA entity.
- Bean Validation on request DTOs with field-level error messages.
- Unique SKU enforced by both an application check and a database constraint.
- Optimistic locking via `@Version`.
- Timestamps (`createdAt`, `updatedAt`) maintained by JPA lifecycle callbacks.
- Sorting is restricted to an allow-list to prevent invalid or unsafe sort properties.
- Page size is capped at 100.
- `spring.jpa.open-in-view` is disabled.
- CORS is enabled for `/api/**`.
- No authentication is included; add Spring Security or an API gateway before exposing this publicly.
