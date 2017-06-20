package ch.jamiete.hilda.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import com.google.gson.JsonObject;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.configuration.Configuration;

public class SQLManager {
    private Connection connection;

    public SQLManager(Hilda hilda) {
        this.init(hilda);
    }

    public Connection getConnection() {
        return this.connection;
    }

    private void init(Hilda hilda) {
        Configuration config = hilda.getConfigurationManager().getConfiguration("sql");

        if (!config.get().has("configured")) {
            JsonObject obj = config.get();
            obj.addProperty("configured", true);
            obj.addProperty("should_connect", false);
            obj.addProperty("host", "localhost:5432");
            obj.addProperty("database", "db");
            obj.addProperty("username", "default");
            obj.addProperty("password", "hunter12");

            config.save();

            Hilda.getLogger().info("Not starting SQL connection as it was not configured. Please configure it.");
            return;
        }

        if (!config.getBoolean("should_connect", false)) {
            return;
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            Hilda.getLogger().severe("Cannot find the PostgreSQL driver. Aborting connection.");
            return;
        }

        String[] required = new String[] { "host", "database", "username", "password" };
        for (String r : required) {
            if (!config.get().has(r)) {
                Hilda.getLogger().warning("Cannot start SQL connection as required configuration options are not present.");
            }
        }

        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(config.getString("host", "localhost:5432"));
        url.append("/");
        url.append(config.getString("database", "db"));

        this.connect(url.toString(), config.getString("username", "default"), config.getString("password", "hunter12"));
    }

    private void connect(String url, String user, String pass) {
        try {
            connection = DriverManager.getConnection(url, user, pass);
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            Hilda.getLogger().log(Level.SEVERE, "Failed to connect to SQL database.", e);
        }
    }

    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

}
