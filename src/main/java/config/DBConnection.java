package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection con;

    private DBConnection() {

    }

    public static Connection getCon() {
        try {
            Class.forName(DatabaseConfig.DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection(DatabaseConfig.CONNECTION_STRING, DatabaseConfig.DB_USER_NAME, DatabaseConfig.DB_PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return con;
    }
}
