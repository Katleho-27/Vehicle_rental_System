package com.example.vehiclerentalsystem.models;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int bookingId;
    private String paymentMethod;
    private double baseAmount;
    private double additionalFees;
    private double lateFees;
    private LocalDateTime paymentDate;
    private String invoiceNumber;

    public Payment(int paymentId, int bookingId, String paymentMethod, double baseAmount, double additionalFees, double lateFees, LocalDateTime paymentDate, String invoiceNumber) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.baseAmount = baseAmount;
        this.additionalFees = additionalFees;
        this.lateFees = lateFees;
        this.paymentDate = paymentDate;
        this.invoiceNumber = invoiceNumber;
    }

    public int getPaymentId() { return paymentId; }
    public int getBookingId() { return bookingId; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getBaseAmount() { return baseAmount; }
    public double getAdditionalFees() { return additionalFees; }
    public double getLateFees() { return lateFees; }
    public double getTotalAmount() { return baseAmount + additionalFees + lateFees; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public String getInvoiceNumber() { return invoiceNumber; }
}
