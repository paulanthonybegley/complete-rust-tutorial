# ============================================================================
#  devops-zero-hero course app — the thing this course teaches you to ship,
#  shipped. Multi-stage: build with Maven, run on a slim JRE.
#
#  Build context is the course root (parent of app/):
#    docker build -t devops-zero-hero:0.0.1 .
# ============================================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY app/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY app/src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /workspace
COPY --from=build /build/target/devops-zero-hero-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]