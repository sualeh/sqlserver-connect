# SQL Server Connection Demo

A complete Maven-based Java application that demonstrates connecting to Microsoft SQL Server using the official Microsoft JDBC driver with support for SQL Server authentication and Active Directory authentication.

## Features

- ✅ Java 17 with Maven build system
- ✅ Microsoft JDBC Driver for SQL Server
- ✅ Environment variable-based configuration
- ✅ Support for SQL Server and Active Directory authentication
- ✅ Docker multi-stage build
- ✅ Docker Compose setup with SQL Server container
- ✅ Connection validation and test query execution
- ✅ Comprehensive error handling and logging

## Prerequisites

- **Docker** and **Docker Compose** installed
- **Java 17** (if running locally without Docker)
- **Maven 3.9+** (if building locally without Docker)

## Project Structure

```
sqlserver-connect/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── SqlServerConnectionDemo.java
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Environment Variables

The application uses the following environment variables for configuration:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SQLSERVER_HOST` | SQL Server hostname | `localhost` | No |
| `SQLSERVER_PORT` | SQL Server port | `1433` | No |
| `SQLSERVER_DB` | Database name | `master` | No |
| `SQLSERVER_USER` | Username for authentication | - | **Yes** |
| `SQLSERVER_PASSWORD` | Password for authentication | - | **Yes** |
| `SQLSERVER_DOMAIN` | AD domain (optional) | - | No |

## Quick Start with Docker Compose

The easiest way to run the demo is using Docker Compose, which will start both SQL Server and the Java application:

```bash
# Option 1: Use default demo credentials (hardcoded in docker-compose.yml)
docker-compose up --build

# Option 2: Use .env file for credentials (recommended)
# Copy the example file and edit with your values
cp .env.example .env
# Edit .env with your preferred credentials
docker-compose up --build

# View logs
docker-compose logs -f app

# Stop and remove containers
docker-compose down
```

**Note:** For production use, always use a `.env` file or Docker secrets instead of hardcoded credentials.

This will:
1. Start a SQL Server 2022 container with SA authentication
2. Build the Java application Docker image
3. Run the application and connect to SQL Server
4. Execute a test query and display results

## Running Locally

### Build the Project

```bash
mvn clean package
```

### Run the Application

Set the required environment variables and run:

```bash
# For SQL Server authentication
export SQLSERVER_HOST=localhost
export SQLSERVER_PORT=1433
export SQLSERVER_DB=master
export SQLSERVER_USER=sa
export SQLSERVER_PASSWORD=Your_strong_Passw0rd!

# Run the application
java -jar target/sqlserver-connect.jar
```

### For Active Directory Authentication

```bash
# For AD authentication with domain
export SQLSERVER_HOST=your-server.domain.com
export SQLSERVER_PORT=1433
export SQLSERVER_DB=YourDatabase
export SQLSERVER_USER=your_username
export SQLSERVER_PASSWORD=your_password
export SQLSERVER_DOMAIN=YOUR_DOMAIN

# Run the application
java -jar target/sqlserver-connect.jar
```

## Docker Build and Run

### Build the Docker Image

```bash
docker build -t sqlserver-connect:latest .
```

### Run the Container

```bash
docker run --rm \
  -e SQLSERVER_HOST=sqlserver \
  -e SQLSERVER_PORT=1433 \
  -e SQLSERVER_DB=master \
  -e SQLSERVER_USER=sa \
  -e SQLSERVER_PASSWORD=Your_strong_Passw0rd! \
  sqlserver-connect:latest
```

## Authentication Methods

### SQL Server Authentication (SA)

This is the default authentication method used in the Docker Compose setup:

```yaml
environment:
  - SQLSERVER_USER=sa
  - SQLSERVER_PASSWORD=Your_strong_Passw0rd!
```

### Active Directory Password Authentication

For Active Directory authentication, set the domain and user credentials:

```yaml
environment:
  - SQLSERVER_USER=your_username
  - SQLSERVER_PASSWORD=your_password
  - SQLSERVER_DOMAIN=YOUR_DOMAIN
```

The application will automatically format the username as `DOMAIN\username` for the JDBC connection.

## Security Notes

⚠️ **Important Security Considerations:**

1. **Never commit passwords** to version control
2. **Use Docker secrets** in production environments
3. **Use environment files** (`.env`) that are gitignored
4. **Rotate credentials** regularly
5. **Use SSL/TLS encryption** in production (`encrypt=true`)
6. **Validate server certificates** in production (set `trustServerCertificate=false`)

### Using Environment Files

Create a `.env` file (not committed to git):

```bash
SQLSERVER_USER=your_user
SQLSERVER_PASSWORD=your_password
```

Reference it in docker-compose.yml:

```yaml
env_file:
  - .env
```

## Troubleshooting

### Connection Failed

1. **Check SQL Server is running:**
   ```bash
   docker-compose ps
   ```

2. **Verify SQL Server is healthy:**
   ```bash
   docker-compose logs sqlserver
   ```

3. **Test connection manually:**
   ```bash
   docker exec -it sqlserver-demo /opt/mssql-tools18/bin/sqlcmd \
     -S localhost -U sa -P 'Your_strong_Passw0rd!' -Q 'SELECT 1' -C
   ```

### Authentication Errors

- Verify username and password are correct
- For AD auth, ensure domain is properly configured
- Check SQL Server allows the authentication method you're using

### Network Issues

- Ensure containers are on the same network
- Check firewall rules if connecting to remote SQL Server
- Verify port 1433 is accessible

## Output Example

When successful, you should see output similar to:

```
=== SQL Server Connection Demo ===

Configuration:
  Host: sqlserver
  Port: 1433
  Database: master
  User: sa
  Domain: (not set)

Connecting to SQL Server...
JDBC URL: jdbc:sqlserver://sqlserver:1433;databaseName=master;encrypt=false;trustServerCertificate=true;user=sa;password=***

✓ Successfully connected to SQL Server!

Executing test query: SELECT 1 AS TestValue
✓ Query executed successfully!
  Result: TestValue = 1

Database Information:
  Product Name: Microsoft SQL Server
  Product Version: 16.00.4095
  Driver Name: Microsoft JDBC Driver 12.6 for SQL Server
  Driver Version: 12.6.1.0
  Catalog: master

=== Connection Demo Completed Successfully ===
```

## License

This project is provided as-is for demonstration purposes.

## Contributing

Feel free to submit issues and enhancement requests!