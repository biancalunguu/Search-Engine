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

    private DatabaseConnection() {}

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
}
