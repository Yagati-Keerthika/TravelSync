package com.travelsync.model;

import java.time.LocalDate;

public class Booking {
    private String bookingCode;
    private String customerName;
    private String destination;
    private LocalDate travelDate;
    private String status;
    private double amount;
    private String notes;

    public Booking() {}

    public Booking(String bookingCode, String customerName, String destination, LocalDate travelDate, String status, double amount) {
        this.bookingCode = bookingCode;
        this.customerName = customerName;
        this.destination = destination;
        this.travelDate = travelDate;
        this.status = status;
        this.amount = amount;
    }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
