package de.qwikbyte.datacore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages the database connection pool and provides utility methods for acquiring and closing database connections.
 * Utilizes a HikariCP connection pool for efficient database connectivity management.
 * Typical use cases include providing connections for executing SQL queries and managing resource cleanup.
 */
public class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(plugin.getConfig().getString("database.url"));
        config.setUsername(plugin.getConfig().getString("database.user"));
        config.setPassword(plugin.getConfig().getString("database.password"));
        config.setMaximumPoolSize(plugin.getConfig().getInt("database.pool-size", 10));
        config.setDriverClassName("org.postgresql.Driver");

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to PostgreSQL database.");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
