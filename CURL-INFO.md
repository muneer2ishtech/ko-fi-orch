#

- Default port: `8080`
- Note: Check and update host and port on which the application is running

- For API names and descriptions:
    - See swagger links in [README.md](./README.md#apis)

# Upload API

## Upload files

Queries source files by criteria and uploads them to object storage.

### Request Details

- URL: `/upload`
- HTTP Method: `POST`
- Content-Type: `application/json`

#### Request JSON fields

| Name     | Required | Description |
|----------|----------|-------------|
| modelId  | Yes      | Model identifier to query source files |
| orderId  | No       | Optional order identifier to narrow the query |
| dateFrom | No       | Start of date range (inclusive); must be set with `dateTo` |
| dateTo   | No       | End of date range (inclusive); must be set with `dateFrom` |

### Response Details

- HTTP Response Code: `200 - OK`
    - Returns which files succeeded and which failed
- HTTP Response Code: `400 - Bad Request`
    - Returned when validation fails (e.g. missing `modelId`, partial date range)

### Response JSON (example — mixed outcome)

```json
{
  "succeeded": [
    "models/MODEL-42/part-a.stp"
  ],
  "failed": [
    {
      "fileName": "models/MODEL-42/part-b.stp",
      "reason": "timeout"
    }
  ]
}
```

### Response JSON (example — no files matched yet)

```json
{
  "succeeded": [],
  "failed": []
}
```

### CURL — model, order, and date range

```sh
curl --request POST --location 'http://localhost:8080/upload' \
--header 'Content-Type: application/json' \
--data '{
  "modelId": "MODEL-42",
  "orderId": "ORD-7",
  "dateFrom": "2026-01-01",
  "dateTo": "2026-01-31"
}'
```

### CURL — model only

```sh
curl --request POST --location 'http://localhost:8080/upload' \
--header 'Content-Type: application/json' \
--data '{
  "modelId": "MODEL-42"
}'
```

### CURL — model and order (no date range)

```sh
curl --request POST --location 'http://localhost:8080/upload' \
--header 'Content-Type: application/json' \
--data '{
  "modelId": "MODEL-42",
  "orderId": "ORD-7"
}'
```

### CURL — validation error (missing modelId)

```sh
curl --request POST --location 'http://localhost:8080/upload' \
--header 'Content-Type: application/json' \
--data '{
  "orderId": "ORD-7"
}'
```

- Expected: `400 Bad Request`

### CURL — validation error (partial date range)

```sh
curl --request POST --location 'http://localhost:8080/upload' \
--header 'Content-Type: application/json' \
--data '{
  "modelId": "MODEL-42",
  "dateFrom": "2026-01-01"
}'
```

- Expected: `400 Bad Request`
