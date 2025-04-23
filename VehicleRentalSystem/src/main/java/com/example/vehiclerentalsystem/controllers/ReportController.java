package com.example.vehiclerentalsystem.controllers;

import com.example.vehiclerentalsystem.Main;
import com.example.vehiclerentalsystem.models.Report;
import com.example.vehiclerentalsystem.models.Vehicle;
import com.example.vehiclerentalsystem.utils.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportController {

    private static final Logger logger = Logger.getLogger(ReportController.class.getName());
    private static final String ERROR_TITLE = "Error";

    @FXML private PieChart availabilityPieChart;
    @FXML private BarChart<String, Number> revenueBarChart;
    @FXML private TableView<Vehicle> availableVehiclesTable;

    @FXML private TableColumn<Vehicle, Integer> colVehicleId;
    @FXML private TableColumn<Vehicle, String> colBrand;
    @FXML private TableColumn<Vehicle, String> colModel;
    @FXML private TableColumn<Vehicle, String> colCategory;
    @FXML private TableColumn<Vehicle, Double> colRentalPricePerDay;

    // Rental History Table + Columns
    @FXML private TableView<Report> rentalHistoryTable;
    @FXML private TableColumn<Report, String> colCustomer;
    @FXML private TableColumn<Report, String> colVehicle;
    @FXML private TableColumn<Report, LocalDate> colStartDate;
    @FXML private TableColumn<Report, LocalDate> colEndDate;
    @FXML private TableColumn<Report, String> colStatus;

    private Connection connect() throws SQLException {
        return DatabaseConnector.getConnection();
    }

    @FXML
    private void handleBackToDashboard() {
        Main.showDashboardView();
    }

    public void initialize() {
        setupAvailableVehiclesTable();
        setupRentalHistoryTable();
        generateAvailableVehiclesReport();
        generateAvailabilityPieChart();
        generateRentalHistory();
    }

    private void setupAvailableVehiclesTable() {
        colVehicleId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colRentalPricePerDay.setCellValueFactory(new PropertyValueFactory<>("rentalPricePerDay"));
    }

    private void setupRentalHistoryTable() {
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colVehicle.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void generateAvailableVehiclesReport() {
        ObservableList<Vehicle> availableVehicles = FXCollections.observableArrayList();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, brand, model, category, rental_price_per_day, is_available FROM vehicles WHERE is_available = 1")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                String category = rs.getString("category");
                double rentalPricePerDay = rs.getDouble("rental_price_per_day");
                boolean isAvailable = rs.getBoolean("is_available");

                availableVehicles.add(new Vehicle(id, brand, model, category, rentalPricePerDay, isAvailable));
            }

            availableVehiclesTable.setItems(availableVehicles);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to generate available vehicles report", e);
            showError("Failed to generate available vehicles report.");
        }
    }

    @FXML
    private void generateRentalHistory() {
        ObservableList<Report> rentalHistories = FXCollections.observableArrayList();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reports")) {

            while (rs.next()) {
                rentalHistories.add(new Report(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getString("customer_name"),
                        rs.getString("vehicle_model"),
                        rs.getString("vehicle_registration"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("payment_date"),
                        rs.getString("status")
                ));
            }

            rentalHistoryTable.setItems(rentalHistories);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to generate rental history", e);
            showError("Failed to generate rental history.");
        }
    }

    @FXML
    private void generateAvailabilityPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT " +
                             "SUM(CASE WHEN is_available = 1 THEN 1 ELSE 0 END) AS available_count, " +
                             "SUM(CASE WHEN is_available = 0 THEN 1 ELSE 0 END) AS rented_count " +
                             "FROM vehicles")) {

            if (rs.next()) {
                int availableCount = rs.getInt("available_count");
                int rentedCount = rs.getInt("rented_count");

                pieChartData.add(new PieChart.Data("Available", availableCount));
                pieChartData.add(new PieChart.Data("Rented", rentedCount));
            }

            availabilityPieChart.setData(pieChartData);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to generate availability pie chart", e);
            showError("Failed to generate availability pie chart.");
        }
    }

    @FXML
    private void generateDailyRevenueReport() {
        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DATE(rental_date) AS day, SUM(price) AS revenue FROM rentals GROUP BY day")) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Revenue");

            while (rs.next()) {
                String day = rs.getString("day");
                double revenue = rs.getDouble("revenue");
                series.getData().add(new XYChart.Data<>(day, revenue));
            }

            data.add(series);
            revenueBarChart.getData().setAll(data);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to generate daily revenue report", e);
            showError("Failed to generate daily revenue report.");
        }
    }

    @FXML
    private void generateWeeklyRevenueReport() {
        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT WEEK(rental_date) AS week, SUM(price) AS revenue FROM rentals GROUP BY week")) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Weekly Revenue");

            while (rs.next()) {
                String week = "Week " + rs.getInt("week");
                double revenue = rs.getDouble("revenue");
                series.getData().add(new XYChart.Data<>(week, revenue));
            }

            data.add(series);
            revenueBarChart.getData().setAll(data);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to generate weekly revenue report", e);
            showError("Failed to generate weekly revenue report.");
        }
    }

    @FXML
    private void generateMonthlyRevenueReport() {
        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT MONTH(rental_date) AS month, SUM(price) AS revenue FROM rentals GROUP BY month")) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Revenue");

            while (rs.next()) {
                String month = "Month " + rs.getInt("month");
                double revenue = rs.getDouble("revenue");
                series.getData().add(new XYChart.Data<>(month, revenue));
            }

            data.add(series);
            revenueBarChart.getData().setAll(data);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to generate monthly revenue report", e);
            showError("Failed to generate monthly revenue report.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
