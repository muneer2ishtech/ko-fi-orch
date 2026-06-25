# ko-fi-orch

Data Orchestration Service for synchronizing files between external systems and cloud storage.

## Tech stack
- Java: 25
- Spring Boot: 4.0.x
- API docs: Springdoc OpenAPI (Swagger UI)
- Containerization: Docker

## Repository

[GitHub](https://github.com/muneer2ishtech/ko-fi-orch)

## Flow

PDM/PLM files are queried from a source system, then uploaded to S3 via presigned URLs:

```
Client → POST /upload (ko-fi-orch)
           → Source API (external): query + download
           → Presign URL API (external): get presigned URL
           → S3: HTTP PUT using that URL
           → response: succeeded / failed per file
```

- External API contracts: see [External APIs](#external-apis) below

## APIs

- For details you can see swagger documentation
    - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    - [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
    - [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

- Note: Check and update URI and PORT on which application is running

| Module | Type   | HTTP | URL      | Description |
|--------|--------|------|----------|-------------|
| Upload | Upload | POST | /upload  | Query source files by criteria and upload to object storage |

- For `curl` and JSON request/response samples:
    - See [CURL-INFO.md](./CURL-INFO.md)

## Build and Run

- Ensure settings are correct in `application.properties`
  - or profile-specific files such as `application-<profile>.properties`

### Maven

#### Local Maven Build

- Build without tests

```
./mvnw clean install -DskipTests
```

- Build with Junit tests

```
./mvnw clean install
```

### Integration tests

- See [INTEGRATION-TESTS.md](./INTEGRATION-TESTS.md)

### Unit tests

- See [UNIT-TESTS.md](./UNIT-TESTS.md)

### External APIs

- PDM/PLM source API — configured via `ko-fi-orch.source.base-url`
- Upload API (presigned URLs) — see [UPLOAD-API.md](./UPLOAD-API.md)

#### Local Maven Run

```
./mvnw spring-boot:run
```

### Docker

- See [DOCKER-BUILD.md](./DOCKER-BUILD.md)
