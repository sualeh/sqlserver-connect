package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL Server Connection Demo
 * 
 * This application demonstrates connecting to SQL Server using the Microsoft JDBC driver
 * with Windows/Active Directory authentication.
 * 
 * Environment Variables:
 * - SQLSERVER_HOST: SQL Server hostname (required)
 * - SQLSERVER_PORT: SQL Server port (default: 1433)
 * - SQLSERVER_DB: Database name (required)
 * - SQLSERVER_USER: Username for authentication (required)
 * - SQLSERVER_PASSWORD: Password for authentication (required)
 * - SQLSERVER_DOMAIN: Optional AD domain for domain authentication
 */
public class SqlServerConnectionDemo {
    
    public static void main(String[] args) {
        System.out.println("=== SQL Server Connection Demo ===");
        System.out.println();
        
        // Read environment variables
        String host = getEnvOrDefault("SQLSERVER_HOST", "localhost");
        String port = getEnvOrDefault("SQLSERVER_PORT", "1433");
        String database = getEnvOrDefault("SQLSERVER_DB", "master");
        String user = System.getenv("SQLSERVER_USER");
        String password = System.getenv("SQLSERVER_PASSWORD");
        String domain = System.getenv("SQLSERVER_DOMAIN");
        
        // Validate required parameters
        if (user == null || user.isEmpty()) {
            System.err.println("ERROR: SQLSERVER_USER environment variable is required");
            System.exit(1);
        }
        
        if (password == null || password.isEmpty()) {
            System.err.println("ERROR: SQLSERVER_PASSWORD environment variable is required");
            System.exit(1);
        }
        
        // Log configuration (without password)
        System.out.println("Configuration:");
        System.out.println("  Host: " + host);
        System.out.println("  Port: " + port);
        System.out.println("  Database: " + database);
        System.out.println("  User: " + user);
        System.out.println("  Domain: " + (domain != null ? domain : "(not set)"));
        System.out.println();
        
        // Build JDBC connection URL
        String jdbcUrl = buildJdbcUrl(host, port, database, user, password, domain);
        System.out.println("Connecting to SQL Server...");
        System.out.println("JDBC URL: " + sanitizeUrl(jdbcUrl));
        System.out.println();
        
        // Attempt connection
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            System.out.println("✓ Successfully connected to SQL Server!");
            System.out.println();
            
            // Execute test query
            executeTestQuery(connection);
            
            // Get database metadata
            printDatabaseInfo(connection);
            
            System.out.println();
            System.out.println("=== Connection Demo Completed Successfully ===");
            
        } catch (SQLException e) {
            System.err.println("✗ Failed to connect to SQL Server!");
            System.err.println();
            System.err.println("Error Details:");
            System.err.println("  Message: " + e.getMessage());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            
            if (e.getCause() != null) {
                System.err.println("  Cause: " + e.getCause().getMessage());
            }
            
            System.err.println();
            System.err.println("Stack Trace:");
            e.printStackTrace();
            
            System.exit(1);
        }
    }
    
    /**
     * Build the JDBC connection URL based on the provided parameters.
     * Supports both standard SQL authentication and Active Directory authentication.
     */
    private static String buildJdbcUrl(String host, String port, String database, 
                                       String user, String password, String domain) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:sqlserver://");
        url.append(host);
        url.append(":");
        url.append(port);
        url.append(";databaseName=").append(database);
        url.append(";encrypt=false");
        url.append(";trustServerCertificate=true");
        
        // Handle domain authentication if domain is specified
        if (domain != null && !domain.isEmpty()) {
            // Use domain-qualified username
            String domainUser = user.contains("@") || user.contains("\\") 
                ? user 
                : domain + "\\" + user;
            url.append(";integratedSecurity=false");
            url.append(";user=").append(domainUser);
            url.append(";password=").append(password);
        } else {
            // Standard SQL authentication
            url.append(";user=").append(user);
            url.append(";password=").append(password);
        }
        
        return url.toString();
    }
    
    /**
     * Sanitize URL for display by removing password
     */
    private static String sanitizeUrl(String url) {
        return url.replaceAll("password=[^;]+", "password=***");
    }
    
    /**
     * Execute a simple test query to verify the connection works
     */
    private static void executeTestQuery(Connection connection) throws SQLException {
        System.out.println("Executing test query: SELECT 1 AS TestValue");
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 AS TestValue")) {
            
            if (rs.next()) {
                int value = rs.getInt("TestValue");
                System.out.println("✓ Query executed successfully!");
                System.out.println("  Result: TestValue = " + value);
            }
        }
        System.out.println();
    }
    
    /**
     * Print database metadata information
     */
    private static void printDatabaseInfo(Connection connection) throws SQLException {
        System.out.println("Database Information:");
        System.out.println("  Product Name: " + connection.getMetaData().getDatabaseProductName());
        System.out.println("  Product Version: " + connection.getMetaData().getDatabaseProductVersion());
        System.out.println("  Driver Name: " + connection.getMetaData().getDriverName());
        System.out.println("  Driver Version: " + connection.getMetaData().getDriverVersion());
        System.out.println("  Catalog: " + connection.getCatalog());
    }
    
    /**
     * Get environment variable or return default value
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
