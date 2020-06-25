package db;
import java.sql.*;

public class DBConnection {
    // Database credentials
    private static final String driverName = "com.mysql.jdbc.Driver";
    private static final String dbUrl = "jdbc:mysql://localhost/mofin";
    private static final String username = "root";
    private static final String password = "";
    private static Connection con;

    public static Connection getConnection() {
        try {
            Class.forName(driverName);
            try {
                con = DriverManager.getConnection(dbUrl, username, password);
            } catch (SQLException ex) {
                // log an exception. fro example:
                System.out.println("Failed to create the database connection."); 
            }
        } catch (ClassNotFoundException ex) {
            // log an exception. for example:
            System.out.println("Driver not found."); 
        }
        return con;
    }
}