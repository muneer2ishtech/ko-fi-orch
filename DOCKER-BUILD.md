## Docker

### Docker build

- Run tests locally before building the image (`./mvnw test`). The Docker build skips tests (`-DskipTests=true` in the Dockerfile).
- Build arg for custom `SERVER_PORT` is optional, you can change to desired value or omit it for default `8080`

```
docker build . \
  -t "muneer2ishtech/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout 2>/dev/null):$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)"

```

- With custom `SERVER_PORT`

```
docker build . \
  --build-arg SERVER_PORT=8181 \
  -t "muneer2ishtech/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout 2>/dev/null):$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)"

```

### Run with docker image

- Note: check and use version from pom.xml
- Add option ` -d` if you want to run in background


- To run on default port and other default settings  
  - E.g.: Default port: `8080`

```
docker run \
  -p 8080:8080 \
  muneer2ishtech/ko-fi-orch:x.y.z
```

- To run by exposing on a different port  
  - Example: expose on `8282` (container still runs on `8080`)

```
docker run \
  -p 8282:8080 \
  muneer2ishtech/ko-fi-orch:x.y.z
```

- To run an image built with custom `SERVER_PORT`  
  - E.g.: built with `SERVER_PORT=8181`, expose on `8282`

```
docker run \
  -p 8282:8181 \
  muneer2ishtech/ko-fi-orch:x.y.z
```
