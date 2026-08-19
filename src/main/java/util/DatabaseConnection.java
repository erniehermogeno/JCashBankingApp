package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // === DATABASE CONFIGURATION ===
    // Protocol://Server:Port/DatabaseName
    private static final String URL = "jdbc:mysql://localhost:3306/jcash_bank_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // === Method: Get a connection to MySQL ===
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Try to connect using the settings above
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            //System.out.println("✅ Connected to database successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Database connection FAILED!");
            System.out.println("Error: " + e.getMessage());
        }
        return conn;
    }

    // === Optional: Test the connection ===
    public static void main(String[] args) {
        getConnection();
    }
}