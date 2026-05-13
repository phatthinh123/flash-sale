# Stage 1: Build all modules
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy gradle wrapper and config files
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Copy all source code (this could be optimized, but works for monorepo)
COPY auth-service/ auth-service/
COPY flashsale-service/ flashsale-service/
COPY inventory-service/ inventory-service/
COPY gateway/ gateway/

# Build all modules
RUN chmod +x gradlew && ./gradlew clean build -x test --no-daemon

# Stage 2: Run specific module
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# We use an argument to know which module's jar to run
ARG MODULE_NAME
ENV MODULE=${MODULE_NAME}

# Copy the built jar from the build stage for the specified module
COPY --from=build /app/${MODULE_NAME}/build/libs/*.jar app.jar

EXPOSE 8080 8081 8082 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
