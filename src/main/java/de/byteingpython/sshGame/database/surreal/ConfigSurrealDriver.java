package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import de.byteingpython.sshGame.config.ConfigurationProvider;

import javax.naming.ConfigurationException;

public class ConfigSurrealDriver extends SyncSurrealDriver {
    public ConfigSurrealDriver(ConfigurationProvider config) throws ConfigurationException {
        super(createConnection(config));
        if (config.getString("SURREALDB_PASSWORD").isPresent() && config.getString("SURREALDB_PASSWORD").isPresent()) {
            this.signIn(config.getString("SURREALDB_USER").get(), config.getString("SURREALDB_PASSWORD").get());
        }
        this.use(config.getString("SURREALDB_NAMESPACE").orElse("default"), config.getString("SURREALDB_NAMESPACE").orElse("default"));
    }

    private static SurrealWebSocketConnection createConnection(ConfigurationProvider config) throws ConfigurationException {
        if (!config.getString("SURREALDB_HOST").isPresent()) {
            throw new ConfigurationException("SurrealDB Host not set");
        }
        SurrealWebSocketConnection conn = new SurrealWebSocketConnection(
                config.getString("SURREALDB_HOST").orElse("localhost"),
                config.getInt("SURREALDB_PORT").orElse(8080),
                config.getBoolean("SURREALDB_SSL").orElse(false));
        conn.connect(config.getInt("SURREALDB_TIMEOUT").orElse(5));
        return conn;
    }
}
