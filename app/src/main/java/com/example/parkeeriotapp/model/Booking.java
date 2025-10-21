package com.example.parkeeriotapp.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;

public class Booking extends RealmObject {
    @PrimaryKey
    private String bookingId = UUID.randomUUID().toString();

    private String userEmail;
    private String mallName;
    private String mallAddress;
    private String slot;
    private String slotId;
    private String plate;
    private String jamMasuk;
    private String jamKeluar;
    private long durasiMenit;
    private int totalHarga;
    private String metodePembayaran;
    private boolean expired;
    private boolean qrScanned = false; // default false

    // Getters and Setters
    public boolean isQrScanned() {
        return qrScanned;
    }

    public void setQrScanned(boolean qrScanned) {
        this.qrScanned = qrScanned;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMallName() {
        return mallName;
    }

    public void setMallName(String mallName) {
        this.mallName = mallName;
    }

    public String getMallAddress() {
        return mallAddress;
    }

    public void setMallAddress(String mallAddress) {
        this.mallAddress = mallAddress;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getJamMasuk() {
        return jamMasuk;
    }

    public void setJamMasuk(String jamMasuk) {
        this.jamMasuk = jamMasuk;
    }

    public String getJamKeluar() {
        return jamKeluar;
    }

    public void setJamKeluar(String jamKeluar) {
        this.jamKeluar = jamKeluar;
    }

    public long getDurasiMenit() {
        return durasiMenit;
    }

    public void setDurasiMenit(long durasiMenit) {
        this.durasiMenit = durasiMenit;
    }

    public int getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(int totalHarga) {
        this.totalHarga = totalHarga;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
