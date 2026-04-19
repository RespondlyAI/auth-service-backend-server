# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
# Copy the gradle wrapper and dependency files first to cache dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
# Make the wrapper executable and download dependencies
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# Copy the actual source code and build
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Extract the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
