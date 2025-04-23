package com.example.vehiclerentalsystem;

import com.example.vehiclerentalsystem.models.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends Application {
    private static Stage primaryStage;
    private static User currentUser;

    // Database credentials
    public static final String DB_URL = "jdbc:mysql://localhost:3306/vehiclerentaldb"; // Replace with your DB URL
    public static final String DB_USERNAME = "root"; // Replace with your DB username
    public static final String DB_PASSWORD = "901017360"; // Replace with your DB password

    // Database connection object
    private static Connection connection;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        connectToDatabase();  // Initialize the database connection
        showLoginView();
    }

    // Connect to the database
    private static void connectToDatabase() {
        try {
            // Establish a connection to the database
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Connected to the database successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database connection failed.");
        }
    }

    // Close the database connection when the application exits
    public static void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error while closing database connection.");
        }
    }

    public static void showLoginView() {
        loadView("LoginView.fxml", "Vehicle Rental System - Login");
    }

    public static void showDashboardView() {
        loadView("DashboardView.fxml", "Dashboard - " + currentUser.getRole());
    }

    public static void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/com/example/vehiclerentalsystem/" + fxmlPath)
            );
            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Provide access to the database connection
    public static Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        // Close the database connection when the application stops
        closeDatabaseConnection();
    }
}
