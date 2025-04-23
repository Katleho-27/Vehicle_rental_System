package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.models.Vehicle;
import com.example.vehiclerentalsystem.utils.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.vehiclerentalsystem.Main;

import java.sql.*;
import java.util.Optional;

public class VehicleController {

    @FXML private TextField vehicleIdField;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField pricePerDayField;
    @FXML private ComboBox<String> availabilityComboBox;
    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, Integer> idColumn;
    @FXML private TableColumn<Vehicle, String> brandColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, String> categoryColumn;
    @FXML private TableColumn<Vehicle, Double> priceColumn;
    @FXML private TableColumn<Vehicle, String> statusColumn;

    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    public void initialize() {
        categoryComboBox.getItems().addAll("Car", "Bike", "Van", "Truck", "SUV", "Motorcycle");
        availabilityComboBox.getItems().addAll("Available", "Unavailable");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("rentalPricePerDay"));
        statusColumn.setCellValueFactory(cellData -> {
            boolean isAvailable = cellData.getValue().isAvailable();
            return new javafx.beans.property.SimpleStringProperty(isAvailable ? "Available" : "Unavailable");
        });

        vehicleTable.setItems(vehicleList);

        loadVehiclesFromDatabase();
    }

    private void loadVehiclesFromDatabase() {
        vehicleList.clear();
        try (Connection conn = DatabaseConnector.getConnection()) {
            String query = "SELECT * FROM vehicles";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int vehicleId = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                String category = rs.getString("category");
                double pricePerDay = rs.getDouble("price_per_day");
                boolean isAvailable = rs.getBoolean("is_available");

                Vehicle v = new Vehicle(vehicleId, brand, model, category, pricePerDay, isAvailable);
                vehicleList.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load vehicles: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddVehicle() {
        try {
            String idText = vehicleIdField.getText().trim();
            String brand = brandField.getText().trim();
            String model = modelField.getText().trim();
            String category = categoryComboBox.getValue();
            String availability = availabilityComboBox.getValue();
            double price = Double.parseDouble(pricePerDayField.getText().trim());

            if (idText.isEmpty() || brand.isEmpty() || model.isEmpty() || category == null || availability == null) {
                showAlert("Error", "Please fill in all fields.");
                return;
            }

            int id = Integer.parseInt(idText);
            boolean isAvailable = availability.equals("Available");

            try (Connection conn = DatabaseConnector.getConnection()) {
                String checkQuery = "SELECT * FROM vehicles WHERE id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    showAlert("Error", "A vehicle with this ID already exists.");
                    return;
                }

                String insertQuery = "INSERT INTO vehicles (id, brand, model, category, price_per_day, is_available) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, id);
                insertStmt.setString(2, brand);
                insertStmt.setString(3, model);
                insertStmt.setString(4, category);
                insertStmt.setDouble(5, price);
                insertStmt.setBoolean(6, isAvailable);

                insertStmt.executeUpdate();
            }

            loadVehiclesFromDatabase();
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid numeric ID and price.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add vehicle: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle != null) {
            try {
                int id = Integer.parseInt(vehicleIdField.getText().trim());
                String brand = brandField.getText().trim();
                String model = modelField.getText().trim();
                String category = categoryComboBox.getValue();
                String availability = availabilityComboBox.getValue();
                double price = Double.parseDouble(pricePerDayField.getText().trim());

                if (brand.isEmpty() || model.isEmpty() || category == null || availability == null) {
                    showAlert("Error", "Please fill in all fields.");
                    return;
                }

                boolean isAvailable = availability.equals("Available");

                try (Connection conn = DatabaseConnector.getConnection()) {
                    if (selectedVehicle.getId() != id) {
                        String checkQuery = "SELECT * FROM vehicles WHERE id = ?";
                        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                        checkStmt.setInt(1, id);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            showAlert("Error", "A vehicle with this ID already exists.");
                            return;
                        }
                    }

                    String updateQuery = "UPDATE vehicles SET id=?, brand=?, model=?, category=?, price_per_day=?, is_available=? WHERE id=?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, id);
                    updateStmt.setString(2, brand);
                    updateStmt.setString(3, model);
                    updateStmt.setString(4, category);
                    updateStmt.setDouble(5, price);
                    updateStmt.setBoolean(6, isAvailable);
                    updateStmt.setInt(7, selectedVehicle.getId());

                    updateStmt.executeUpdate();
                }

                loadVehiclesFromDatabase();
                clearFields();

            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid numeric ID and price.");
            } catch (SQLException e) {
                showAlert("Error", "Failed to update vehicle: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                showAlert("Error", "Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Please select a vehicle to update.");
        }
    }

    @FXML
    private void handleDeleteVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Vehicle");
            alert.setContentText("Are you sure you want to delete this vehicle?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.getConnection()) {
                    String deleteQuery = "DELETE FROM vehicles WHERE id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, selectedVehicle.getId());
                    deleteStmt.executeUpdate();
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete vehicle: " + e.getMessage());
                    e.printStackTrace();
                }
                loadVehiclesFromDatabase();
                clearFields();
            }
        } else {
            showAlert("Error", "Please select a vehicle to delete.");
        }
    }

    @FXML
    private void handleTableClick() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle != null) {
            vehicleIdField.setText(String.valueOf(selectedVehicle.getId()));
            brandField.setText(selectedVehicle.getBrand());
            modelField.setText(selectedVehicle.getModel());
            categoryComboBox.setValue(selectedVehicle.getCategory());
            pricePerDayField.setText(String.valueOf(selectedVehicle.getRentalPricePerDay()));
            availabilityComboBox.setValue(selectedVehicle.isAvailable() ? "Available" : "Unavailable");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        Main.showDashboardView();
    }

    private void clearFields() {
        vehicleIdField.clear();
        brandField.clear();
        modelField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        pricePerDayField.clear();
        availabilityComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
