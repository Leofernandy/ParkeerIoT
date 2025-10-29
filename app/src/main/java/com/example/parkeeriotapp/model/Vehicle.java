package com.example.parkeeriotapp.model;

public class Vehicle {
    private String plate;
    private String brand;
    private String model;
    private String year;
    private String color;

    public Vehicle() {} // diperlukan untuk Firestore

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}