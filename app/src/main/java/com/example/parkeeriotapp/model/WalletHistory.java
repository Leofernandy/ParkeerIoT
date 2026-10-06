package com.example.parkeeriotapp.model;

public class WalletHistory {
    private boolean isHeader;
    private String headerDate;
    private String title;
    private String subtitle;
    private String amount;
    private String date;
    private int type; // 1 = Top Up (+), 2 = Payment (-), 3 = Refund (+)
    private long timestamp;

    // Header constructor
    public WalletHistory(String headerDate) {
        this.isHeader = true;
        this.headerDate = headerDate;
    }

    // Full Item constructor
    public WalletHistory(String title, String subtitle, String amount, String date, int type, long timestamp) {
        this.isHeader = false;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Backward compatible constructor
    public WalletHistory(String title, String amount, String date, int type, long timestamp) {
        this(title, "Transaksi", amount, date, type, timestamp);
    }

    public boolean isHeader() { return isHeader; }
    public String getHeaderDate() { return headerDate; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public String getDate() { return date; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }
}