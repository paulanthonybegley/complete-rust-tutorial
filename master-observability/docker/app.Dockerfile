# ---- build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# dependencies first (cache-friendly)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# the OpenTelemetry Java agent (JAR artifact straight from Maven Central, no curl)
RUN mvn -q -B dependency:copy \
    -Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.12.0 \
    -DoutputDirectory=/build/agent
RUN mv /build/agent/opentelemetry-javaagent-2.12.0.jar /build/agent/agent.jar

COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- runtime stage ---------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar /app/app.jar
COPY --from=build /build/agent/agent.jar /app/agent.jar
EXPOSE 8080 8081
ENTRYPOINT ["java", "-javaagent:/app/agent.jar", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]