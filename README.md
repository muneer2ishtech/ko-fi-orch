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

## Design

Key decisions and rationale (concise):

- **Ports and adapters** — `SourceQueryPort`, `SourceDownloadPort`, `PresignUrlPort`, `UploadPort` isolate externals; orchestrator depends on interfaces, not HTTP/AWS details
- **Single orchestration service** — `FileUploadOrchestratorImpl` coordinates query → download → presign → S3 PUT; controller stays thin
- **External contracts assumed** — PDM/PLM source and presign URL APIs are documented in [SOURCE-API.md](./SOURCE-API.md) and [UPLOAD-API.md](./UPLOAD-API.md); adapters can be swapped when real APIs are known
- **Presigned URL only from external API** — production uses `RestUploadPresignUrlAdapter`; no `S3Client.putObject` in main code (assignment: upload via presigned URL)
- **S3 upload via HTTP PUT** — `S3PresignedUploadAdapter` + `RestClient` PUTs bytes to the URL returned by presign API
- **Per-file failure handling** — one file failing does not stop others; response lists `succeeded` and `failed` with reasons
- **Configuration via properties** — `ko-fi-orch.source.*`, `ko-fi-orch.upload-api.*`, `ko-fi-orch.s3.*`; overridable with env vars for environments
- **Synchronous sequential processing** — simple flow for assignment scope; async/queue noted as future improvement for long runs
- **`/api/v1` on external APIs** — versioned paths on systems we call; ko-fi-orch endpoint is `POST /upload` (can move to `/api/v1/uploads` later)
- **AWS SDK presigner for tests only** — `S3PresignUrlAdapter` supports LocalStack integration tests; not a Spring bean in production
- **Unit tests with mocks** — ports tested independently (`Mockito`, `MockRestServiceServer`); see [UNIT-TESTS.md](./UNIT-TESTS.md)

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

Assumed contracts for systems **called by** `ko-fi-orch`

- PDM/PLM **source API** — query + download — see [SOURCE-API.md](./SOURCE-API.md) (`ko-fi-orch.source.base-url`)
- **Presign URL API** — returns presigned S3 PUT URLs only — see [UPLOAD-API.md](./UPLOAD-API.md) (`ko-fi-orch.upload-api.base-url`)

#### Local Maven Run

```
./mvnw spring-boot:run
```

### Docker

- See [DOCKER-BUILD.md](./DOCKER-BUILD.md)
