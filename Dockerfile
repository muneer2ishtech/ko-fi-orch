# ====== Stage 1: Build ======
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY . .

ARG MAVEN_CLI_OPTS="-B -q"

RUN chmod +x ./mvnw

RUN ./mvnw $MAVEN_CLI_OPTS clean package -DskipTests=true

# ====== Stage 2: Runtime ======
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/ko-fi-orch-*.jar ko-fi-orch.jar

# For building image with custom ports and properties
ARG SERVER_PORT=8080
ENV SERVER_PORT=${SERVER_PORT}
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java", "-jar", "ko-fi-orch.jar"]
