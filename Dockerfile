# ==========================================
# Stage 1: Build stage using Maven & Java 17
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build executable Spring Boot JAR (skipping tests as tests run in CI)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Secure, lightweight runtime stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a dedicated non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from the builder stage
COPY --from=builder /build/target/campus-lost-found-api-0.0.1-SNAPSHOT.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to unprivileged runtime user
USER appuser

# Expose API port
EXPOSE 8080

# Environment variables for JVM tuning
ENV JAVA_OPTS="-Xms128m -Xmx256m"

# Application entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
