package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton: one shared JDBC connection reused across all DAOs instead of
 * every DAO call opening its own. Reconnects transparently if the held
 * connection has been closed or dropped.
 */
public final class DBConnection {

    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/sunrise_dental_clinic");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
