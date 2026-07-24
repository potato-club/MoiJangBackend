# Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts ./
COPY src src

RUN chmod +x gradlew \
  && ./gradlew bootJar -x test --no-daemon

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/build/libs/moi-jang-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
