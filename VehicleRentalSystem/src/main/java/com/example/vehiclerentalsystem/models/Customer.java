package com.example.vehiclerentalsystem.models;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String name;
    private String contactInfo;
    private String drivingLicenseNumber;
    private List<String> rentalHistory;

    public Customer(String name, String contactInfo, String drivingLicenseNumber) {
        this.name = name;
        this.contactInfo = contactInfo;
        this.drivingLicenseNumber = drivingLicenseNumber;
        this.rentalHistory = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    public List<String> getRentalHistory() {
        return rentalHistory;
    }

    public void addRentalToHistory(String rentalInfo) {
        this.rentalHistory.add(rentalInfo);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", drivingLicenseNumber='" + drivingLicenseNumber + '\'' +
                '}';
    }
}