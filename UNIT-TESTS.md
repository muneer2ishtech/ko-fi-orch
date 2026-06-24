## Unit tests

Unit tests run **without** external APIs or Docker. Dependencies are mocked (`Mockito`, `MockRestServiceServer`, `@WebMvcTest`).

- For Docker / LocalStack tests, see [INTEGRATION-TESTS.md](./INTEGRATION-TESTS.md)
- Update this doc when adding or changing unit tests

### Run

- All tests (unit + integration)

```
./mvnw test
```

- Unit tests only (exclude integration)

```
./mvnw test -Dtest='!S3PresignUploadIntegrationTest,!S3PresignUploadLocalStackManualTest'
```

- Single test class

```
./mvnw test -Dtest=UploadControllerTest
```

### Coverage

#### `KoFiOrchApplicationTests`

| Test | Type | What it verifies |
|------|------|------------------|
| `contextLoads` | Happy | Spring application context starts |

#### `UploadControllerTest`

| Test | Type | What it verifies |
|------|------|------------------|
| `uploadReturnsOrchestratorResult` | Happy | `POST /upload` returns 200 and maps `succeeded` / `failed` from orchestrator |
| `uploadRejectsMissingModelId` | Negative | 400 when `modelId` is missing |
| `uploadRejectsPartialDateRange` | Negative | 400 when only one of `dateFrom` / `dateTo` is set |
| `uploadRejectsInvertedDateRange` | Negative | 400 when `dateFrom` is after `dateTo` |

**Gaps:** blank `modelId`, malformed JSON, model-only request (no dates) as explicit happy path

#### `FileUploadOrchestratorImplTest`

| Test | Type | What it verifies |
|------|------|------------------|
| `returnsEmptyResponseUntilSourceIntegrationIsWired` | Happy | Stub orchestrator returns empty `succeeded` / `failed` |

**Gaps:** failure paths when source query, download, presign, or upload fail (needs real wiring)

#### `S3PresignUrlAdapterTest`

| Test | Type | What it verifies |
|------|------|------------------|
| `presignsPutObjectUrlForFile` | Happy | Builds presign request with bucket, key, content type, duration; returns `PresignedUrl` |
| `defaultsContentTypeWhenMissing` | Happy | Uses `application/octet-stream` when file content type is blank |

**Gaps:** no negative case (e.g. presigner throws)

#### `S3PresignedUploadAdapterTest`

| Test | Type | What it verifies |
|------|------|------------------|
| `uploadsFileContentViaHttpPut` | Happy | PUTs bytes to presigned URL; returns success |
| `returnsFailureResultWhenS3RespondsWithError` | Negative | HTTP 500 → `UploadResult.failure` (non-2xx via `RestClient`) |

**Gaps:** 4xx response, signed-headers path

#### `S3UploadServiceTest`

| Test | Type | What it verifies |
|------|------|------------------|
| `uploadPresignsThenUploadsFile` | Happy | Calls presign port then upload port; returns success |

**Gaps:** upload failure from `UploadPort`, presign failure from `PresignUrlPort`

### Summary

| Test class | Happy | Negative |
|------------|------:|---------:|
| `KoFiOrchApplicationTests` | 1 | 0 |
| `UploadControllerTest` | 1 | 3 |
| `FileUploadOrchestratorImplTest` | 1 | 0 |
| `S3PresignUrlAdapterTest` | 2 | 0 |
| `S3PresignedUploadAdapterTest` | 1 | 1 |
| `S3UploadServiceTest` | 1 | 0 |
| **Total** | **7** | **4** |
