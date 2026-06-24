# ko-fi-orch

Data Orchestration Service for synchronizing files between external systems and cloud storage.

## Tech stack
- Java: 25
- Spring Boot: 4.0.x
- Containerization: Docker

## Repository

[GitHub](https://github.com/muneer2ishtech/ko-fi-orch)

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

#### Local Maven Run

```
./mvnw spring-boot:run
```

### Docker

- See [DOCKER-BUILD.md](./DOCKER-BUILD.md)
