## Integration tests

S3 presign + upload integration tests use **LocalStack** (S3) via Docker.

### Prerequisites

- Docker Desktop (or Docker Engine) running and reachable from the JVM
- Testcontainers version in `pom.xml` is **1.21.4** or higher (required for Docker Desktop 29.x)

### Docker images

Images are pulled automatically on first run (no manual `docker pull` needed):

- `localstack/localstack:4.4.0` — S3 for presign + upload tests
- `testcontainers/ryuk:0.12.0` — Testcontainers cleanup (started automatically)

### Tests

| Test | How LocalStack is started |
|------|---------------------------|
| `S3PresignUploadIntegrationTest` | Testcontainers starts `localstack/localstack:4.4.0` |
| `S3PresignUploadLocalStackManualTest` | You start LocalStack on `localhost:4566` (see below) |

- If Docker is not available, `S3PresignUploadIntegrationTest` is **skipped** (`@EnabledIfDockerAvailable`)
- `S3PresignUploadLocalStackManualTest` is **skipped** when nothing is listening on `http://localhost:4566`

### Run

- All tests (unit + integration)

```
./mvnw test
```

- Integration test only (Testcontainers)

```
./mvnw test -Dtest=S3PresignUploadIntegrationTest
```

- Manual LocalStack test only

```
./mvnw test -Dtest=S3PresignUploadLocalStackManualTest
```

### Manual LocalStack (optional)

Use this only for `S3PresignUploadLocalStackManualTest`.

- Start LocalStack

```
docker run -d --name localstack-kone -p 4566:4566 localstack/localstack:4.4.0
```

- Stop and remove when done

```
docker stop localstack-kone && docker rm localstack-kone
```

- Note: `S3PresignUploadIntegrationTest` does **not** need this container; it starts its own via Testcontainers
