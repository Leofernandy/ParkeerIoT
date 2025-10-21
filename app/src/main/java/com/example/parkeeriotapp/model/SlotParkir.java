package com.example.parkeeriotapp.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SlotParkir extends RealmObject {

    @PrimaryKey
    private String slotId; // A-01_1 (slot + mallId)
    private String slotName; // A-01
    private boolean isBooked;
    private int mallId;

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotName() { return slotName; }
    public void setSlotName(String slotName) { this.slotName = slotName; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    public int getMallId() { return mallId; }
    public void setMallId(int mallId) { this.mallId = mallId; }
}

