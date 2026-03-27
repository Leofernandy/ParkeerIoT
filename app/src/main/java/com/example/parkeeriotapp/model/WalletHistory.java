package com.example.parkeeriotapp.model;

public class WalletHistory {
    private String title;
    private String amount;
    private String date;
    private int type;
    private long timestamp; // <-- TAMBAHAN BARU

    // Constructor (Tambahkan long timestamp di ujung)
    public WalletHistory(String title, String amount, String date, int type, long timestamp) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.timestamp = timestamp; // <-- SIMPAN DISINI
    }

    public String getTitle() { return title; }
    public String getAmount() { return amount; }
    public String getDate() { return date; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; } // <-- GETTER BARU
}