package searchengine;

import searchengine.db.DatabaseConnection;
import searchengine.indexing.IndexingService;
import searchengine.ui.SearchApplication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Local File Search Engine");
        System.out.println();

        if (!connectToDatabase()) {
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                IndexingService indexingService = new IndexingService();
                indexingService.run();
            } else if (choice.equals("2")) {
                startSearch();
            } else if (choice.equals("3")) {
                running = false;
                System.out.println("Goodbye!");
            } else {
                System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }

            System.out.println();
        }

        DatabaseConnection.getInstance().close();
        scanner.close();
    }

    private static boolean connectToDatabase() {
        try {
            DatabaseConnection.getInstance().getConnection();
            System.out.println("Database connected successfully.");
            System.out.println();
            return true;
        } catch (SQLException e) {
            System.out.println("Could not connect to the database.");
            System.out.println("Check db.url, db.username and db.password in config.properties.");
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private static void startSearch() {
        try {
            SearchApplication searchApplication = new SearchApplication();
            searchApplication.run();
        } catch (SQLException e) {
            System.out.println("Could not start search.");
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showMenu() {
        System.out.println("1. Index files");
        System.out.println("2. Search");
        System.out.println("3. Exit");
        System.out.print("> ");
    }
}