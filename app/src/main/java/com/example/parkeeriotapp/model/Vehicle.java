package com.example.parkeeriotapp.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Vehicle extends RealmObject {
    @PrimaryKey
    private String plate;
    private String brand;
    private String model;
    private String year;
    private String color;

    private String ownerEmail;

    // Getter & Setter
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

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
