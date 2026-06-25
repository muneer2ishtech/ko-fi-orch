## Source API (external)

PDM/PLM source system — query file metadata and download file content. ko-fi-orch does **not** host this API; it calls it as a client.

- Configuration: `ko-fi-orch.source.base-url` in `application.properties`
- Default: `http://localhost:9090` (override with env `SOURCE_API_BASE_URL`)
- Implementation: `RestSourceQueryAdapter` (`SourceQueryPort`), `RestSourceDownloadAdapter` (`SourceDownloadPort`)

### Assumed contract

#### Query files

- URL: `{baseUrl}/api/v1/files`
- HTTP Method: `GET`

**Query parameters**

| Name     | Required | Description |
|----------|----------|-------------|
| modelId  | Yes      | Model identifier to search PDM/PLM documents |
| orderId  | No       | Optional order identifier to narrow results |
| dateFrom | No       | Start of date range (inclusive); must be set with `dateTo` |
| dateTo   | No       | End of date range (inclusive); must be set with `dateFrom` |

**Response JSON**

```json
{
  "files": [
    {
      "fileId": "doc-1001",
      "fileName": "models/MODEL-42/part-a.stp",
      "contentType": "application/octet-stream",
      "revision": "A",
      "lastModified": "2026-01-15"
    }
  ]
}
```

| Field        | Required | Description |
|--------------|----------|-------------|
| fileId       | Yes      | Source document identifier (used for download) |
| fileName     | Yes      | File path or name (used as S3 object key) |
| contentType  | No       | MIME type; defaults to octet-stream when blank on download |
| revision     | No       | Document revision (e.g. CAD revision) |
| lastModified | No       | Release or last-modified date |

#### Download file content

- URL: `{baseUrl}/api/v1/files/{fileId}/content`
- HTTP Method: `GET`
- Response: raw file bytes (`Content-Type` from metadata or response header)

### Example curl (against a running source API stub)

Query:

```sh
curl --request GET --location \
  'http://localhost:9090/api/v1/files?modelId=MODEL-42&orderId=ORD-7&dateFrom=2026-01-01&dateTo=2026-01-31'
```

Download:

```sh
curl --request GET --location \
  'http://localhost:9090/api/v1/files/doc-1001/content' \
  --output part-a.stp
```
