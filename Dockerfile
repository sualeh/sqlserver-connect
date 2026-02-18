# Multi-stage Dockerfile for SQL Server Connection Demo
# Stage 1: Build stage using Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Verify the JAR was created
RUN ls -lh /build/target/*.jar

# Stage 2: Runtime stage with JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/sqlserver-connect.jar /app/sqlserver-connect.jar

# Verify the JAR was copied
RUN ls -lh /app/*.jar

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "/app/sqlserver-connect.jar"]

# Health check (optional)
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD java -version || exit 1

# Add labels for metadata
LABEL maintainer="SQL Server Connection Demo"
LABEL description="Java application for connecting to SQL Server"
LABEL version="1.0.0"
