package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.models.Booking;
import com.example.vehiclerentalsystem.utils.DatabaseConnector;
import com.example.vehiclerentalsystem.Main;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BookingController {

    @FXML private ComboBox<String> vehicleComboBox;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, String> customerColumn;
    @FXML private TableColumn<Booking, String> vehicleColumn;
    @FXML private TableColumn<Booking, LocalDate> startDateColumn;
    @FXML private TableColumn<Booking, LocalDate> endDateColumn;
    @FXML private TableColumn<Booking, String> statusColumn;

    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private Map<String, Integer> customerNameToId = new HashMap<>();
    private Map<String, String> vehicleNameToId = new HashMap<>();

    public void initialize() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        customerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCustomerName()
                ));
        vehicleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getVehicleName()
                ));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isActive() ? "Active" : "Cancelled"
                ));

        bookingTable.setItems(bookingList);
        loadCustomers();
        loadVehicles();
        loadBookingsFromDatabase();
    }

    private void loadCustomers() {
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM customers")) {

            ObservableList<String> customerNames = FXCollections.observableArrayList();
            while (rs.next()) {
                int customerId = rs.getInt("id");
                String customerName = rs.getString("name");
                customerNames.add(customerName);
                customerNameToId.put(customerName, customerId);
            }
            customerComboBox.setItems(customerNames);

        } catch (Exception e) {
            showAlert("Database Error", "Could not load customers: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, model FROM vehicles")) {

            ObservableList<String> vehicleNames = FXCollections.observableArrayList();
            while (rs.next()) {
                String vehicleId = rs.getString("id");
                String vehicleModel = rs.getString("model");
                vehicleNames.add(vehicleModel);
                vehicleNameToId.put(vehicleModel, vehicleId);
            }
            vehicleComboBox.setItems(vehicleNames);

        } catch (Exception e) {
            showAlert("Database Error", "Could not load vehicles: " + e.getMessage());
        }
    }

    private void loadBookingsFromDatabase() {
        bookingList.clear();
        String query = "SELECT b.id, b.vehicle_id, v.model AS vehicle_name, " +
                "b.customer_id, c.name AS customer_name, " +
                "b.start_date, b.end_date, b.active " +
                "FROM bookings b " +
                "JOIN vehicles v ON b.vehicle_id = v.id " +
                "JOIN customers c ON b.customer_id = c.id";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int bookingId = rs.getInt("id");
                String vehicleId = rs.getString("vehicle_id");
                String vehicleName = rs.getString("vehicle_name");
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("customer_name");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                boolean active = rs.getBoolean("active");

                Booking booking = new Booking(bookingId, vehicleId, vehicleName, customerId, customerName, startDate, endDate, active);
                bookingList.add(booking);
            }

        } catch (Exception e) {
            showAlert("Database Error", "Could not load bookings: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddBooking() {
        try {
            String selectedVehicleName = vehicleComboBox.getValue();
            String selectedCustomerName = customerComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (selectedVehicleName == null || selectedCustomerName == null || startDate == null || endDate == null) {
                showAlert("Error", "Please fill in all fields.");
                return;
            }

            if (endDate.isBefore(startDate)) {
                showAlert("Error", "End date must be after start date.");
                return;
            }

            String vehicleId = vehicleNameToId.get(selectedVehicleName);
            int customerId = customerNameToId.get(selectedCustomerName);

            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO bookings (vehicle_id, customer_id, start_date, end_date, active) VALUES (?, ?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, vehicleId);
                stmt.setInt(2, customerId);
                stmt.setDate(3, Date.valueOf(startDate));
                stmt.setDate(4, Date.valueOf(endDate));
                stmt.setBoolean(5, true);

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newBookingId = generatedKeys.getInt(1);
                        Booking newBooking = new Booking(newBookingId, vehicleId, selectedVehicleName,
                                customerId, selectedCustomerName, startDate, endDate, true);
                        bookingList.add(newBooking);
                    }
                }
            }

            clearFields();

        } catch (Exception e) {
            showAlert("Error", "Failed to add booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBooking() {
        Booking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Cancellation");
            alert.setHeaderText("Cancel Booking");
            alert.setContentText("Are you sure you want to cancel this booking?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE bookings SET active = false WHERE id = ?")) {

                    stmt.setInt(1, selectedBooking.getBookingId());
                    stmt.executeUpdate();

                    selectedBooking.setActive(false);
                    bookingTable.refresh();

                } catch (Exception e) {
                    showAlert("Error", "Failed to cancel booking: " + e.getMessage());
                }
            }
        } else {
            showAlert("Error", "Please select a booking to cancel.");
        }
    }

    @FXML
    private void handleTableClick() {
        Booking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
        if (selectedBooking != null) {
            vehicleComboBox.getSelectionModel().select(selectedBooking.getVehicleName());
            customerComboBox.getSelectionModel().select(selectedBooking.getCustomerName());
            startDatePicker.setValue(selectedBooking.getStartDate());
            endDatePicker.setValue(selectedBooking.getEndDate());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        Main.showDashboardView();
    }

    private void clearFields() {
        vehicleComboBox.getSelectionModel().clearSelection();
        customerComboBox.getSelectionModel().clearSelection();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
