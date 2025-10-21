package com.example.parkeeriotapp.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Mall extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private String address;
    private String distance;
    private int imageResId;
    private int pricePerHour;

    private double latitude;
    private double longitude;

    public Mall() {

    }

    public Mall(int id, String name, String address, String distance, int imageResId, int pricePerHour, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.imageResId = imageResId;
        this.pricePerHour = pricePerHour;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDistance() { return distance; }
    public int getImageResId() { return imageResId; }
    public int getPricePerHour() { return pricePerHour; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setDistance(String distance) { this.distance = distance; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setPricePerHour(int pricePerHour) { this.pricePerHour = pricePerHour; }
}
