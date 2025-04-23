package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.Main;
import com.example.vehiclerentalsystem.models.Payment;
import com.example.vehiclerentalsystem.utils.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

public class PaymentController {

    @FXML private ComboBox<String> bookingComboBox;
    @FXML private TextField baseAmountField;
    @FXML private TextField additionalFeesField;
    @FXML private TextField lateFeesField;
    @FXML private TextField totalAmountField;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TableView<Payment> paymentTable;

    @FXML private TableColumn<Payment, Integer> paymentIdColumn;
    @FXML private TableColumn<Payment, Integer> bookingIdColumn;
    @FXML private TableColumn<Payment, String> methodColumn;
    @FXML private TableColumn<Payment, Double> baseAmountColumn;
    @FXML private TableColumn<Payment, Double> additionalFeesColumn;
    @FXML private TableColumn<Payment, Double> lateFeesColumn;
    @FXML private TableColumn<Payment, Double> totalAmountColumn;
    @FXML private TableColumn<Payment, String> dateColumn;
    @FXML private TableColumn<Payment, String> invoiceColumn;

    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();

    private Connection connect() throws SQLException {
        return DatabaseConnector.getConnection();
    }

    public void initialize() {
        paymentMethodComboBox.getItems().addAll("Cash", "Credit Card", "Online Payment");

        paymentIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        baseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("baseAmount"));
        additionalFeesColumn.setCellValueFactory(new PropertyValueFactory<>("additionalFees"));
        lateFeesColumn.setCellValueFactory(new PropertyValueFactory<>("lateFees"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentDate().toString()));
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

        loadBookings();   // Load bookings into ComboBox
        loadPayments();   // Load existing payments into TableView
    }

    // For loading bookings
    private void loadBookings() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM bookings")) {  // Use 'id' here instead of 'booking_id'

            ObservableList<String> bookingIds = FXCollections.observableArrayList();

            while (rs.next()) {
                int bookingId = rs.getInt("id");  // Getting the 'id' from 'bookings' table
                bookingIds.add("B" + bookingId);  // Add booking id to the ComboBox
            }

            bookingComboBox.setItems(bookingIds);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load booking IDs.");
        }
    }

    private void loadPayments() {
        paymentList.clear();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM payments")) {
            while (rs.next()) {
                Payment payment = new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("booking_id"),
                        rs.getString("payment_method"),
                        rs.getDouble("base_amount"),
                        rs.getDouble("additional_fees"),
                        rs.getDouble("late_fees"),
                        rs.getTimestamp("payment_date").toLocalDateTime(),
                        rs.getString("invoice_number")
                );
                paymentList.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        paymentTable.setItems(paymentList);
    }

    @FXML
    private void handleCalculateTotal() {
        try {
            double baseAmount = parseDoubleOrZero(baseAmountField.getText());
            double additionalFees = parseDoubleOrZero(additionalFeesField.getText());
            double lateFees = parseDoubleOrZero(lateFeesField.getText());

            totalAmountField.setText(String.format("%.2f", baseAmount + additionalFees + lateFees));
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid amounts.");
        }
    }

    private double parseDoubleOrZero(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;  // Return 0 if the input is invalid
        }
    }

    @FXML
    private void handleAddPayment() {
        try {
            String selectedBooking = bookingComboBox.getValue();
            String paymentMethod = paymentMethodComboBox.getValue();
            double baseAmount = parseDoubleOrZero(baseAmountField.getText());
            double additionalFees = parseDoubleOrZero(additionalFeesField.getText());
            double lateFees = parseDoubleOrZero(lateFeesField.getText());
            double totalAmount = parseDoubleOrZero(totalAmountField.getText());

            if (selectedBooking == null || paymentMethod == null || baseAmount <= 0 || totalAmount <= 0) {
                showAlert("Error", "Please fill in all required fields.");
                return;
            }

            // Extract booking ID like "B12" → 12
            int bookingId = Integer.parseInt(selectedBooking.replace("B", "").split(" ")[0]);

            String invoiceNumber = "INV-" + (new Random().nextInt(900000) + 100000);
            LocalDateTime paymentDate = LocalDateTime.now();

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO payments (booking_id, payment_method, base_amount, additional_fees, late_fees, total_amount, payment_date, invoice_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, bookingId);
                pstmt.setString(2, paymentMethod);
                pstmt.setDouble(3, baseAmount);
                pstmt.setDouble(4, additionalFees);
                pstmt.setDouble(5, lateFees);
                pstmt.setDouble(6, totalAmount);
                pstmt.setTimestamp(7, Timestamp.valueOf(paymentDate));
                pstmt.setString(8, invoiceNumber);
                pstmt.executeUpdate();
            }

            showAlert("Success", "Payment processed successfully!\nInvoice #: " + invoiceNumber);
            clearFields();
            loadPayments();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Invalid input format.");
        }
    }


    @FXML
    private void handlePrintInvoice() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment != null) {
            String invoice = generateInvoiceText(selectedPayment);
            showAlert("Invoice", invoice);
        } else {
            showAlert("Error", "Please select a payment to print invoice.");
        }
    }

    private String generateInvoiceText(Payment payment) {
        return String.format(
                "INVOICE #%s\n\nBooking ID: B%d\nPayment Date: %s\n\nBase Amount: $%.2f\nAdditional Fees: $%.2f\nLate Fees: $%.2f\n------------------\nTOTAL: $%.2f\n\nPayment Method: %s\n\nThank you for your business!",
                payment.getInvoiceNumber(), payment.getBookingId(), payment.getPaymentDate(),
                payment.getBaseAmount(), payment.getAdditionalFees(), payment.getLateFees(),
                payment.getTotalAmount(), payment.getPaymentMethod()
        );
    }

    @FXML
    private void handleBackToDashboard() {
        Main.showDashboardView();
    }

    private void clearFields() {
        bookingComboBox.getSelectionModel().clearSelection();
        baseAmountField.clear();
        additionalFeesField.clear();
        lateFeesField.clear();
        totalAmountField.clear();
        paymentMethodComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
