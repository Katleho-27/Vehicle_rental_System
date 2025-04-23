package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Button vehiclesButton;
    @FXML private Button customersButton;
    @FXML private Button bookingsButton;
    @FXML private Button paymentsButton;
    @FXML private Button reportsButton;
    @FXML private Label welcomeLabel;

    public void initialize() {
        // Set welcome message with user role
        welcomeLabel.setText("Welcome, " + Main.getCurrentUser().getUsername() +
                " (" + Main.getCurrentUser().getRole() + ")");

        // Apply role-based access control
        if (Main.getCurrentUser().getRole().equals("Employee")) {
            vehiclesButton.setDisable(true);
            customersButton.setDisable(true);
            reportsButton.setDisable(true);
        }
    }

    @FXML
    private void openVehicles() {
        Main.loadView("VehicleView.fxml", "Vehicle Management");
    }

    @FXML
    private void openCustomers() {
        Main.loadView("CustomerView.fxml", "Customer Management");
    }

    @FXML
    private void openBookings() {
        Main.loadView("BookingView.fxml", "Booking Management");
    }

    @FXML
    private void openPayments() {
        Main.loadView("PaymentView.fxml", "Payment Management");
    }

    @FXML
    private void openReports() {
        Main.loadView("ReportView.fxml", "Reports");
    }

    @FXML
    private void handleLogout() {
        Main.setCurrentUser(null);
        Main.showLoginView();
    }
}