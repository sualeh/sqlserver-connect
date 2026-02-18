# Dockerfile for SQL Server Connection Demo
# This Dockerfile uses a pre-built JAR file
# To build the JAR first, run: mvn clean package

# Use JRE for runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the pre-built JAR
# Build the JAR first with: mvn clean package
COPY target/sqlserver-connect.jar /app/sqlserver-connect.jar

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
