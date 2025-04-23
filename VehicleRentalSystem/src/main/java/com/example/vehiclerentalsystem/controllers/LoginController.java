package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.models.User;
import com.example.vehiclerentalsystem.utils.DatabaseConnector;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.example.vehiclerentalsystem.Main;

import java.sql.*;
import java.util.Objects;

public class LoginController {
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private ComboBox<String> roleComboBox;

    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Employee");
    }

    @FXML
    private void showRegister() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }

    @FXML
    private void showLogin() {
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (Objects.equals(password, dbPassword)) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    Main.setCurrentUser(user);
                    Main.showDashboardView();
                } else {
                    showAlert("Error", "Invalid password");
                }
            } else {
                showAlert("Error", "Username not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Error", "Please fill all fields!");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            // Check for existing username
            String checkQuery = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                showAlert("Error", "Username already exists!");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            insertStmt.executeUpdate();

            showAlert("Success", "Registered successfully! Now log in.");
            showLogin();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
