package service;

import model.Transaction;
import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.ResultSet;

public class WalletService {

    // === CASH-IN FEATURE ===
    public String cashIn(User user, Scanner input) {
        System.out.println("\n===== CASH-IN =====");
        System.out.print("Enter amount to add: ₱");

        double amount;
        try {
            amount = Double.parseDouble(input.nextLine().trim());
        } catch (NumberFormatException e) {
            return "❌ Please enter a valid number!";
        }

        // Validate: must be positive
        if (amount <= 0) {
            return "❌ Amount must be GREATER THAN 0!";
        }

        // Calculate new balance
        double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);

        // Save to DATABASE
        boolean saved = updateUserBalance(user.getUserId(), newBalance);
        if (!saved) {
            return "⚠️ Error: Could not update balance in database!";
        }

        // Record TRANSACTION LOG
        Transaction t = new Transaction("CASH-IN", amount, "Funds loaded to wallet");
        saveTransaction(user.getUserId(), t);
        user.addTransaction(t);

        return "✅ Cash-In SUCCESSFUL!\n" +
                "Added: ₱" + amount + "\n" +
                "New Balance: ₱" + newBalance;
    }

    // === HELPER: Update balance in MySQL ===
    private boolean updateUserBalance(long userId, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBalance);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0; // ✅ Returns true if updated

        } catch (SQLException e) {
            System.out.println("⚠️ Balance update error: " + e.getMessage());
            return false;
        }
    }

    // === HELPER: Save transaction to MySQL ===
    public void saveTransaction(long userId, Transaction t) {
        String sql = "INSERT INTO transactions (user_id, type, amount, details, date_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, t.getType());
            stmt.setDouble(3, t.getAmount());
            stmt.setString(4, t.getDetails());
            stmt.setString(5, t.getDateTime());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("⚠️ Transaction save error: " + e.getMessage());
        }
    }

    // === SHOW CURRENT BALANCE ===
    public String showBalance(User user) {
        return "\n💰 Your Current Balance: ₱" + user.getBalance();
    }
    // === TRANSFER FEATURE ===
    public String transfer(User sender, Scanner input) {
        System.out.println("\n===== TRANSFER MONEY =====");

        System.out.print("Enter Receiver's Mobile Number: ");
        String receiverMobile = input.nextLine().trim();

        // Step 1: Find receiver in database
        User receiver = findUserByMobile(receiverMobile);
        if (receiver == null) {
            return "❌ Receiver with that mobile number NOT FOUND!";
        }

        // Cannot send to YOURSELF
        if (sender.getUserId() == receiver.getUserId()) {
            return "❌ You cannot transfer to YOURSELF!";
        }

        // Step 2: Get & validate amount
        System.out.print("Enter Amount to Transfer: ₱");
        double amount;
        try {
            amount = Double.parseDouble(input.nextLine().trim());
        } catch (NumberFormatException e) {
            return "❌ Please enter a valid number!";
        }

        if (amount <= 0) {
            return "❌ Amount must be GREATER THAN 0!";
        }

        // Step 3: Check sender balance
        if (sender.getBalance() < amount) {
            return "❌ INSUFFICIENT BALANCE!\n" +
                    "Your Balance: ₱" + sender.getBalance() + "\n" +
                    "Amount Needed: ₱" + amount;
        }

        // Step 4: Perform the transfer
        double senderNewBalance = sender.getBalance() - amount;
        double receiverNewBalance = receiver.getBalance() + amount;

        // Update BOTH balances in database
        boolean success = updateBothBalances(
                sender.getUserId(), senderNewBalance,
                receiver.getUserId(), receiverNewBalance
        );

        if (!success) {
            return "⚠️ Transfer FAILED! Please try again.";
        }

        // Update local objects
        sender.setBalance(senderNewBalance);
        receiver.setBalance(receiverNewBalance);

        // Step 5: Record TRANSACTION LOGS (BOTH sides!)
        String detailsSent = "Sent to: " + receiver.getFullName() + " (" + receiverMobile + ")";
        Transaction sentTx = new Transaction("TRANSFER SENT", amount, detailsSent);
        saveTransaction(sender.getUserId(), sentTx);
        sender.addTransaction(sentTx);

        String detailsReceived = "Received from: " + sender.getFullName() + " (" + sender.getMobileNumber() + ")";
        Transaction receivedTx = new Transaction("TRANSFER RECEIVED", amount, detailsReceived);
        saveTransaction(receiver.getUserId(), receivedTx);

        // Return SUCCESS message
        return "✅ TRANSFER SUCCESSFUL!\n" +
                "Sent to: " + receiver.getFullName() + "\n" +
                "Amount: ₱" + amount + "\n" +
                "Your New Balance: ₱" + sender.getBalance();
    }

    // === HELPER: Find user by mobile number ===
    public User findUserByMobile(String mobileNumber) {
        String sql = "SELECT * FROM users WHERE mobile_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mobileNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setMobileNumber(rs.getString("mobile_number"));
                user.setPin(rs.getString("pin"));
                user.setFullName(rs.getString("full_name"));
                user.setBalance(rs.getDouble("balance"));
                return user;
            }

        } catch (SQLException e) {
            System.out.println("⚠️ Find user error: " + e.getMessage());
        }
        return null;
    }

    // === HELPER: Update BOTH balances at once ===
    private boolean updateBothBalances(long senderId, double senderNewBal, long receiverId, double receiverNewBal) {
        String updateSender = "UPDATE users SET balance = ? WHERE user_id = ?";
        String updateReceiver = "UPDATE users SET balance = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction — ALL or NOTHING ✅

            // Deduct from sender
            try (PreparedStatement stmt1 = conn.prepareStatement(updateSender)) {
                stmt1.setDouble(1, senderNewBal);
                stmt1.setLong(2, senderId);
                stmt1.executeUpdate();
            }

            // Add to receiver
            try (PreparedStatement stmt2 = conn.prepareStatement(updateReceiver)) {
                stmt2.setDouble(1, receiverNewBal);
                stmt2.setLong(2, receiverId);
                stmt2.executeUpdate();
            }

            conn.commit(); // Save BOTH changes
            return true;

        } catch (SQLException e) {
            System.out.println("⚠️ Transfer error: " + e.getMessage());
            return false;
        }
    }
    // === VIEW TRANSACTION HISTORY ===
    public String viewTransactionHistory(User user) {
        System.out.println("\n===== TRANSACTION HISTORY =====");

        // Load fresh transactions from database
        java.util.List<Transaction> history = getTransactionsForUser(user.getUserId());

        if (history.isEmpty()) {
            return "📋 No transactions found yet!";
        }

        StringBuilder output = new StringBuilder();
        output.append("📋 Total Transactions: ").append(history.size()).append("\n");
        output.append("----------------------------------------\n");

        for (Transaction t : history) {
            output.append(t.toString()).append("\n");
        }

        output.append("----------------------------------------\n");
        output.append("Current Balance: ₱").append(user.getBalance());

        return output.toString();
    }

    // === HELPER: Load transactions from DATABASE ===
    private java.util.List<Transaction> getTransactionsForUser(long userId) {
        java.util.List<Transaction> list = new java.util.ArrayList<>();
        String sql = "SELECT type, amount, details, date_time FROM transactions " +
                "WHERE user_id = ? ORDER BY date_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction();
                t.setType(rs.getString("type"));
                t.setAmount(rs.getDouble("amount"));
                t.setDetails(rs.getString("details"));
                t.setDateTime(rs.getString("date_time"));
                list.add(t);
            }

        } catch (SQLException e) {
            System.out.println("⚠️ Load transactions error: " + e.getMessage());
        }
        return list;
    }
}