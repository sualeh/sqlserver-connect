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

## Active Directory and Microsoft Entra (Azure AD) Authentication

### Certificate Requirements

**Do I need to provide or set up any certificates on my Docker image?**

**Short Answer:** For Active Directory/Entra authentication with SSL encryption enabled (recommended for production), your Docker image needs to have up-to-date CA certificates installed. Most modern base images already include these, but you may need to update them or configure custom certificates.

### Detailed Requirements

#### 1. SSL/TLS Encryption
- **Development/Testing:** The current demo uses `encrypt=false` and `trustServerCertificate=true` for simplicity
- **Production:** You **MUST** use `encrypt=true` for Active Directory and Entra authentication to Azure SQL Database
- When SSL is enabled, the JDBC driver validates the server's certificate against trusted root CAs

#### 2. Certificate Trust Store
The Microsoft JDBC Driver uses Java's default trust store located at `$JAVA_HOME/lib/security/cacerts`.

**In Docker:**
- Most OpenJDK base images (like `eclipse-temurin`) include CA certificates by default
- The Alpine-based images include the `ca-certificates` package
- If you encounter certificate validation errors, ensure certificates are up-to-date

**To install/update CA certificates in Docker:**

```dockerfile
# For Alpine-based images (used in this project)
RUN apk update && apk add --no-cache ca-certificates

# For Debian/Ubuntu-based images
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*
```

#### 3. Custom Trust Store Configuration

If you need to use a custom trust store (e.g., for self-signed certificates or internal CAs):

```dockerfile
# Copy your custom trust store
COPY mycacerts /app/security/cacerts

# Set Java system properties when running the application
ENV JAVA_OPTS="-Djavax.net.ssl.trustStore=/app/security/cacerts -Djavax.net.ssl.trustStorePassword=changeit"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/sqlserver-connect.jar"]
```

Or use runtime configuration:

```bash
docker run --rm \
  -e SQLSERVER_HOST=your-server.database.windows.net \
  -e SQLSERVER_DB=YourDatabase \
  -e SQLSERVER_USER=user@domain.com \
  -e SQLSERVER_PASSWORD=your_password \
  -e JAVA_OPTS="-Djavax.net.ssl.trustStore=/custom/cacerts" \
  sqlserver-connect:latest
```

#### 4. Additional Dependencies for Microsoft Entra Authentication

⚠️ **Important:** The current version of this demo supports traditional Active Directory authentication with domain credentials. For **Microsoft Entra (Azure AD)** authentication modes like `ActiveDirectoryPassword`, `ActiveDirectoryManagedIdentity`, or `ActiveDirectoryDefault`, you need additional libraries:

**Required additional dependencies:**
- `com.microsoft.azure:msal4j` (Microsoft Authentication Library for Java)
- Other transitive dependencies

**To add Entra authentication support, update `pom.xml`:**

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>msal4j</artifactId>
    <version>1.13.10</version>
</dependency>
```

**And modify the JDBC connection string to use Entra authentication:**

```java
// For Azure AD Password authentication
String jdbcUrl = "jdbc:sqlserver://your-server.database.windows.net:1433;" +
    "databaseName=YourDatabase;" +
    "authentication=ActiveDirectoryPassword;" +
    "user=user@domain.com;" +
    "password=your_password;" +
    "encrypt=true;";

// For Azure AD Managed Identity (no password needed)
String jdbcUrl = "jdbc:sqlserver://your-server.database.windows.net:1433;" +
    "databaseName=YourDatabase;" +
    "authentication=ActiveDirectoryManagedIdentity;" +
    "encrypt=true;";
```

### Authentication Method Comparison

| Authentication Type | SSL Required | CA Certificates Needed | Additional Libraries | Use Case |
|-------------------|--------------|----------------------|---------------------|----------|
| **SQL Server Authentication** | Recommended | Yes (if SSL enabled) | None | Local SQL Server, basic auth |
| **Windows/AD Domain** | Recommended | Yes (if SSL enabled) | None | On-premises AD with domain |
| **Azure AD Password** | **Required** | **Yes** | MSAL4J | Azure SQL with AD credentials |
| **Azure AD Managed Identity** | **Required** | **Yes** | MSAL4J | Azure resources with managed identity |
| **Azure AD Default** | **Required** | **Yes** | MSAL4J | Multi-method fallback (MI, env, etc.) |

### Troubleshooting Certificate Issues

#### Error: "The driver could not establish a secure connection to SQL Server"

**Solution:**
1. Verify CA certificates are installed in your Docker image
2. Check that the server certificate is valid and not expired
3. Ensure the hostname in the JDBC URL matches the certificate's Common Name (CN) or Subject Alternative Name (SAN)

```bash
# Test certificate validation
openssl s_client -connect your-server.database.windows.net:1433 -showcerts
```

#### Error: "PKIX path building failed" or "unable to find valid certification path"

**Solution:**
1. The Java trust store doesn't contain the required root CA certificate
2. Import the missing certificate into the trust store:

```bash
# Get the certificate from the server
openssl s_client -connect your-server:1433 -showcerts < /dev/null | \
  openssl x509 -outform PEM > server-cert.pem

# Import into Java cacerts (may need to do this in Dockerfile)
keytool -import -alias sqlserver -file server-cert.pem \
  -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt
```

#### Error: "Login failed for user" with Azure AD

**Solution:**
1. Ensure you're using the correct authentication mode (`authentication=ActiveDirectoryPassword`)
2. Use the UPN format for the username: `user@domain.com` (not `DOMAIN\user`)
3. Verify MSAL4J library is on the classpath
4. Check that SSL encryption is enabled (`encrypt=true`)

### Production Checklist for Active Directory/Entra Authentication

- [ ] SSL encryption is enabled (`encrypt=true`)
- [ ] Server certificate validation is enabled (`trustServerCertificate=false`)
- [ ] CA certificates are installed and up-to-date in the Docker image
- [ ] MSAL4J library is included if using Azure AD authentication
- [ ] Username format is correct (UPN for Azure AD: `user@domain.com`)
- [ ] Custom trust store is configured if using internal/self-signed certificates
- [ ] Connection string includes the correct `authentication` parameter for Azure AD
- [ ] Firewall rules allow outbound connections on port 1433
- [ ] Network connectivity to Active Directory/Azure AD endpoints is verified

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