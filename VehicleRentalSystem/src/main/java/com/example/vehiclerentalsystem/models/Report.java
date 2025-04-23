package com.example.vehiclerentalsystem.models;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Report {
    private int id;
    private int bookingId;
    private String customerName;
    private String vehicleModel;
    private String vehicleRegistration;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalAmount;
    private Timestamp paymentDate;
    private String status;

    public Report(int id, int bookingId, String customerName, String vehicleModel, String vehicleRegistration,
                  LocalDate startDate, LocalDate endDate, double totalAmount, Timestamp paymentDate, String status) {
        this.id = id;
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.vehicleModel = vehicleModel;
        this.vehicleRegistration = vehicleRegistration;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public String getVehicleModel() { return vehicleModel; }
    public String getVehicleRegistration() { return vehicleRegistration; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalAmount() { return totalAmount; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
    public void setStatus(String status) { this.status = status; }
}
