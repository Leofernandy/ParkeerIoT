package com.example.parkeeriotapp.model;

// Pastikan nama paket (package) di atas sesuai dengan struktur folder Anda

import com.google.firebase.database.IgnoreExtraProperties;

// @IgnoreExtraProperties
public class Booking {
    // Nama variabel HARUS SAMA PERSIS dengan key di Realtime Database
    private String bookingId;
    private String mallId;
    private String mallName;
    private String mallAddress;
    private String slot;
    private String plate;
    private String jamMasuk;
    private String jamKeluar;
    private long durasiMenit; // Gunakan long, Firebase RTDB menyimpan angka sebagai Long
    private long totalHarga; // Gunakan long
    private String status;
    private String userId;

    // Diperlukan constructor kosong untuk Firebase DataSnapshot.getValue(Booking.class)
    public Booking() {
    }

    // --- Getters ---
    public String getBookingId() { return bookingId; }
    public String getMallId() { return mallId; }
    public String getMallName() { return mallName; }
    public String getMallAddress() { return mallAddress; }
    public String getSlot() { return slot; }
    public String getPlate() { return plate; }
    public String getJamMasuk() { return jamMasuk; }
    public String getJamKeluar() { return jamKeluar; }
    public long getDurasiMenit() { return durasiMenit; }
    public long getTotalHarga() { return totalHarga; }
    public String getStatus() { return status; }
    public String getUserId() { return userId; }

    // --- Setters ---
    // (Firebase juga terkadang butuh setters, jadi kita tambahkan)
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setMallId(String mallId) { this.mallId = mallId; }
    public void setMallName(String mallName) { this.mallName = mallName; }
    public void setMallAddress(String mallAddress) { this.mallAddress = mallAddress; }
    public void setSlot(String slot) { this.slot = slot; }
    public void setPlate(String plate) { this.plate = plate; }
    public void setJamMasuk(String jamMasuk) { this.jamMasuk = jamMasuk; }
    public void setJamKeluar(String jamKeluar) { this.jamKeluar = jamKeluar; }
    public void setDurasiMenit(long durasiMenit) { this.durasiMenit = durasiMenit; }
    public void setTotalHarga(long totalHarga) { this.totalHarga = totalHarga; }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(String userId) { this.userId = userId; }
}
