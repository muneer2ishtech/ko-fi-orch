## Presign URL API (external)

The orchestration service obtains **presigned S3 upload URLs** from an external upload API (not by calling AWS directly).

- Configuration: `ko-fi-orch.upload-api.base-url` in `application-xxx.properties`
- Default: `http://localhost:9091` (override with env `UPLOAD_API_BASE_URL`)
- Implementation: `RestUploadPresignUrlAdapter` (`PresignUrlPort`)

### Assumed contract

#### Presign upload URL

- URL: `{baseUrl}/api/v1/presigned-upload-urls`
- HTTP Method: `POST`
- Content-Type: `application/json`

**Request JSON**

| Field         | Required | Description |
|---------------|----------|-------------|
| fileName      | Yes      | Object key / file name for the upload |
| contentType   | Yes      | MIME type; defaults to `application/octet-stream` when blank |

```json
{
  "fileName": "models/MODEL-42/part-a.stp",
  "contentType": "application/octet-stream"
}
```

**Response JSON**

| Field         | Required | Description |
|---------------|----------|-------------|
| fileName      | Yes      | File name the URL is valid for |
| url           | Yes      | Presigned URL to PUT file bytes to |
| signedHeaders | No       | Headers required on the PUT request; omit or `{}` when none |

```json
{
  "fileName": "models/MODEL-42/part-a.stp",
  "url": "https://bucket.s3.amazonaws.com/models/MODEL-42/part-a.stp?X-Amz-Signature=...",
  "signedHeaders": {
    "Content-Type": ["application/octet-stream"]
  }
}
```

After presign, file bytes are uploaded with `S3PresignedUploadAdapter` (HTTP PUT to the returned URL).

### LocalStack / AWS SDK presign

`S3PresignUrlAdapter` (AWS SDK `S3Presigner`) is kept for **integration tests** only; it is not registered as a Spring bean. Production flow uses this upload API.

### Example curl (against a running upload API stub)

```sh
curl --request POST --location 'http://localhost:9091/api/v1/presigned-upload-urls' \
--header 'Content-Type: application/json' \
--data '{
  "fileName": "models/MODEL-42/part-a.stp",
  "contentType": "application/octet-stream"
}'
```
