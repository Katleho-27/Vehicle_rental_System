package com.example.vehiclerentalsystem.models;

public class Vehicle {
    private int id;
    private String brand;
    private String model;
    private String category;
    private double rentalPricePerDay;
    private boolean isAvailable;

    public Vehicle(int id, String brand, String model, String category, double rentalPricePerDay, boolean isAvailable) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.rentalPricePerDay = rentalPricePerDay;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public double getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
