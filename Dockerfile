# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy pom.xml for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests --batch-mode

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

# Set Java options
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=40.0 -XX:InitialRAMPercentage=25.0 -XX:+ExitOnOutOfMemoryError"

# Copy jar
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

# Run as non-root
USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
