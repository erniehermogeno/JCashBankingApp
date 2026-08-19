package service;

import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class LoginService {
    private static final int MAX_ATTEMPTS = 3;
    private User loggedInUser;

    // === MAIN LOGIN METHOD ===
    public User login(Scanner input) {
        int attempts = 0;
        loggedInUser = null;

        System.out.println("\n===== JCash Bank Login =====");

        // Loop until success OR 3 failed attempts
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter Mobile Number: ");
            String mobile = input.nextLine().trim();

            System.out.print("Enter PIN: ");
            String pin = input.nextLine().trim();

            // Try to verify from database
            loggedInUser = verifyUser(mobile, pin);

            if (loggedInUser != null) {
                System.out.println("\n✅ Login SUCCESSFUL!");
                System.out.println("Welcome, " + loggedInUser.getFullName() + "!");
                return loggedInUser;  // Exit — logged in!
            } else {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                System.out.println("❌ Incorrect mobile number or PIN.");

                if (remaining > 0) {
                    System.out.println("Attempts left: " + remaining);
                }
            }
        }

        // Here = 3 failed attempts
        System.out.println("\n🚫 TOO MANY FAILED ATTEMPTS. Access locked.");
        return null;
    }

    // === HELPER: Check mobile + PIN in DATABASE ===
    private User verifyUser(String mobileNumber, String pin) {
        String sql = "SELECT * FROM users WHERE mobile_number = ? AND pin = ?";

        // Try-with-resources — Auto-closes connection! (NC III Optimization ✅)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Replace ? with actual values
            stmt.setString(1, mobileNumber);
            stmt.setString(2, pin);

            ResultSet rs = stmt.executeQuery();

            // If found → Build User object
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setMobileNumber(rs.getString("mobile_number"));
                user.setPin(rs.getString("pin"));
                user.setFullName(rs.getString("full_name"));
                user.setBalance(rs.getDouble("balance"));
                return user; // ✅ Found! Return the User!
            }

        } catch (SQLException e) {
            System.out.println("⚠️ Database error during login: " + e.getMessage());
        }

        return null; // ❌ Not found
    }

    // === Get who is logged in ===
    public User getLoggedInUser() {
        return loggedInUser;
    }
}