package ch.jamiete.hilda;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;

public class HildaDB {
    private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String MAX_POOL = "250";

    private Connection con;
    private Properties properties;

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
        }
        return properties;
    }

    Connection connect() {
        if (con == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                con = DriverManager.getConnection(DATABASE_URL, getProperties());
                Hilda.getLogger().info("Connected to database!");
            } catch (ClassNotFoundException | SQLException e) {
                Hilda.getLogger().log(Level.SEVERE, "Error has occurred when connecting to the database:", e);
            }
        }
        return con;
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                Hilda.getLogger().log(Level.SEVERE, "Error has occurred when disconnecting from the database:", e);
            }
        }
    }

    public void updateDb(String s) {
        try {
            PreparedStatement pst;
            pst = this.connect().prepareStatement("insert into messages (messagescol)" + "VALUES (?)");
            pst.setString(1,s);
            pst.executeUpdate();
        } catch (SQLException e) {
            Hilda.getLogger().log(Level.SEVERE, "Error has occurred when updating the database:", e);
        } finally {
            this.disconnect();
        }
    }
}
