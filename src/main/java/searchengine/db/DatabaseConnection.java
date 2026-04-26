package searchengine.db;

import searchengine.config.ConfigurationManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton that manages a single JDBC connection to MySQL.
 *
 * Why a single connection instead of a pool?
 * The indexer and the query engine are never used simultaneously,
 * so one connection is sufficient for this project.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    public DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns a live connection, re-opening it if it was closed or never opened.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            ConfigurationManager cfg = ConfigurationManager.getInstance();
            String url = cfg.getProperty("db.url");
            String username = cfg.getProperty("db.username");
            String password = cfg.getProperty("db.password");
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }

    /**
     * Cleanly closes the connection when the application exits.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[INFO] Database connection closed.");
            } catch (SQLException e) {
                System.err.println("[WARN] Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Reads a SQL script file and executes its statements.
     */
    public void initializeDatabase(String scriptPath) {
        try {
            String sql = java.nio.file.Files.readString(java.nio.file.Paths.get(scriptPath));
            String[] statements = sql.split(";");
            Connection conn = getConnection();
            try (java.sql.Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement);
                    }
                }
            }
            System.out.println("[INFO] Database schema initialized successfully.");
        } catch (Exception e) {
            System.err.println("[WARN] Failed to run schema script: " + e.getMessage());
        }
    }
}
