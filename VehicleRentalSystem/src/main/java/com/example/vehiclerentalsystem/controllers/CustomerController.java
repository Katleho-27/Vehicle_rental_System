package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.models.Customer;
import com.example.vehiclerentalsystem.utils.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.Optional;

public class CustomerController {

    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField licenseField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> contactColumn;
    @FXML private TableColumn<Customer, String> licenseColumn;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
        licenseColumn.setCellValueFactory(new PropertyValueFactory<>("drivingLicenseNumber"));

        loadCustomersFromDatabase();
    }

    private void loadCustomersFromDatabase() {
        customerList.clear();
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {

            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getString("name"),
                        rs.getString("contact_info"),
                        rs.getString("driving_license_number")
                );
                customerList.add(customer);
            }

            customerTable.setItems(customerList);
            customerTable.refresh();

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load customers: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCustomer() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String license = licenseField.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || license.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT * FROM customers WHERE driving_license_number = ?");
            checkStmt.setString(1, license);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert("Error", "A customer with this license number already exists.");
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO customers (name, contact_info, driving_license_number) VALUES (?, ?, ?)");
            insertStmt.setString(1, name);
            insertStmt.setString(2, contact);
            insertStmt.setString(3, license);
            insertStmt.executeUpdate();

            loadCustomersFromDatabase();
            clearFields();

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add customer: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert("Error", "Please select a customer to update.");
            return;
        }

        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String license = licenseField.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || license.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (!selectedCustomer.getDrivingLicenseNumber().equals(license)) {
                PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT * FROM customers WHERE driving_license_number = ?");
                checkStmt.setString(1, license);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showAlert("Error", "A customer with this license number already exists.");
                    return;
                }
            }

            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE customers SET name = ?, contact_info = ?, driving_license_number = ? WHERE driving_license_number = ?");
            updateStmt.setString(1, name);
            updateStmt.setString(2, contact);
            updateStmt.setString(3, license);
            updateStmt.setString(4, selectedCustomer.getDrivingLicenseNumber());
            updateStmt.executeUpdate();

            loadCustomersFromDatabase();
            clearFields();

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update customer: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert("Error", "Please select a customer to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Customer");
        alert.setContentText("Are you sure you want to delete this customer?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnector.getConnection()) {
                PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM customers WHERE driving_license_number = ?");
                deleteStmt.setString(1, selectedCustomer.getDrivingLicenseNumber());
                deleteStmt.executeUpdate();

                loadCustomersFromDatabase();
                clearFields();

            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete customer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTableClick() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            nameField.setText(selectedCustomer.getName());
            contactField.setText(selectedCustomer.getContactInfo());
            licenseField.setText(selectedCustomer.getDrivingLicenseNumber());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        com.example.vehiclerentalsystem.Main.showDashboardView();
    }

    private void clearFields() {
        nameField.clear();
        contactField.clear();
        licenseField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
