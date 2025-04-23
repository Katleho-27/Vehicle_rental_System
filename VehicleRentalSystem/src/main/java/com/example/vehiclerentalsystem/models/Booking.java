package com.example.vehiclerentalsystem.models;

import java.time.LocalDate;

public class Booking {
    private int bookingId;
    private String vehicleId;
    private String vehicleName;
    private int customerId;
    private String customerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    // Full constructor
    public Booking(int bookingId, String vehicleId, String vehicleName, int customerId, String customerName,
                   LocalDate startDate, LocalDate endDate, boolean active) {
        this.bookingId = bookingId;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    // Getters and Setters

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
