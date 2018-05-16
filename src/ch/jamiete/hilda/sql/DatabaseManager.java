package ch.jamiete.hilda.sql;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ch.jamiete.hilda.Hilda;
import ch.jamiete.hilda.configuration.Configuration;

public class DatabaseManager {
    private final Hilda hilda;
    private HikariDataSource ds;

    public DatabaseManager(Hilda hilda) {
        this.hilda = hilda;
        this.init();
    }

    public boolean isAvailable() {
        if (this.ds == null) {
            return false;
        }

        return this.ds.isRunning();
    }

    public void init() {
        Hilda.getLogger().info("Initiating database connections...");

        if (this.ds != null) {
            Hilda.getLogger().info("Closing current connections to database...");
            this.ds.close();
        }

        Configuration hcfg = hilda.getConfigurationManager().getConfiguration("hilda-database");

        if (!hcfg.getBoolean("enable", false)) {
            Hilda.getLogger().info("Not loading database.");
            return;
        }

        HikariConfig dcfg = new HikariConfig();

        // Connection settings

        StringBuilder url = new StringBuilder();
        url.append("jdbc:").append("mysql://");
        url.append(hcfg.getString("hostname", "localhost")).append(":");
        url.append(hcfg.getString("port", "3306")).append("/");
        url.append(hcfg.getString("database", "hilda"));

        dcfg.setJdbcUrl(url.toString());
        dcfg.setUsername(hcfg.getString("username", "hilda"));
        dcfg.setPassword(hcfg.getString("password", "51mp50n"));

        // Pool settings

        dcfg.setMinimumIdle(3);

        // Timezone settings

        dcfg.addDataSourceProperty("serverTimezone", "UTC");

        // MySQL settings

        dcfg.addDataSourceProperty("cachePrepStmts", true);
        dcfg.addDataSourceProperty("prepStmtCacheSize", 250);
        dcfg.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        dcfg.addDataSourceProperty("useServerPrepStmts", true);
        dcfg.addDataSourceProperty("useLocalSessionState", true);
        dcfg.addDataSourceProperty("cacheServerConfiguration", true);
        dcfg.addDataSourceProperty("elideSetAutoCommits", true);
        dcfg.addDataSourceProperty("maintainTimeStats", false);

        // Establish

        this.ds = new HikariDataSource(dcfg);

        Hilda.getLogger().info("Completed loading database. Database is " + (this.isAvailable() ? "now available" : "**not** available") + ".");
    }

    public Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }

    public HikariDataSource getDataSource() {
        return this.ds;
    }

}
