/*
 * ==========================================
 * JCash E-Money Banking Application
 * Author: ERNIE SERNA HERMOGENO
 * Date: August 2026
 * ==========================================
 */

import model.User;
import service.LoginService;
import service.WalletService;

import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        LoginService loginService = new LoginService();
        WalletService walletService = new WalletService();

        System.out.println("==================================");
        System.out.println("   Welcome to JCash E-money App");
        System.out.println("==================================");

        // === LOGIN ===
        User currentUser = loginService.login(input);

        if (currentUser == null) {
            System.out.println("\n🚫 Could not log in. Exiting...");
            input.close();
            return;
        }

        // === MAIN MENU ===
        while (true) {
            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Check Balance");
            System.out.println("2. Cash-In (Add Funds)");
            System.out.println("3. Transfer Money");
            System.out.println("4. Transaction History");
            System.out.println("0. Exit");
            System.out.println("================================");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println(walletService.showBalance(currentUser));
                    break;

                case "2":
                    String result = walletService.cashIn(currentUser, input);
                    System.out.println(result);
                    break;

                case "3":
                    String transferResult = walletService.transfer(currentUser, input);
                    System.out.println(transferResult);
                    break;

                case "4":
                    String history = walletService.viewTransactionHistory(currentUser);
                    System.out.println(history);
                    break;

                case "0":
                    System.out.println("👋 Thank you for using JCash Bank! Goodbye!");
                    input.close();
                    return;

                default:
                    System.out.println("⚠️ Invalid option! Please try again.");
            }
        }
    }
}