package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    // === ATTRIBUTES (PRIVATE = ENCAPSULATION ✅) ===
    private String type;        // e.g., "CASH-IN", "TRANSFER SENT", "TRANSFER RECEIVED"
    private double amount;      // Amount of money in this transaction
    private String details;     // Description or notes (e.g., "To 09171234567")
    private String dateTime;    // When it happened

    // === CONSTRUCTORS ===
    // Empty constructor
    public Transaction() {
    }

    // Full constructor — auto-sets current date/time
    public Transaction(String type, double amount, String details) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        // Auto-capture date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.dateTime = now.format(formatter);
    }

    // === GETTERS ===
    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDetails() {
        return details;
    }

    public String getDateTime() {
        return dateTime;
    }

    // === SETTERS ===
    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    // === Optional: Show transaction as readable text ===
    @Override
    public String toString() {
        return "[" + dateTime + "] " + type + " | Amount: ₱" + amount + " | " + details;
    }
}