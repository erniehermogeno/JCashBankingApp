package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private long userId;
    private String mobileNumber;
    private String pin;
    private String fullName;
    private double balance;
    private List<Transaction> transactions;  // Transaction history

    // === CONSTRUCTOR — creating a new User ===
    public User() {
        // Empty constructor
        this.transactions = new ArrayList<>();
    }

    public User(long userId, String mobileNumber, String pin, String fullName, double balance) {
        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.pin = pin;
        this.fullName = fullName;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    // === GETTERS — read the values ===
    public long getUserId() {
        return userId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getPin() {
        return pin;
    }

    public String getFullName() {
        return fullName;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    // === SETTERS — change the values ===
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // === Helper method: add a transaction to the list ===
    public void addTransaction(Transaction t) {
        this.transactions.add(t);
    }
}