package com.example.parkeeriotapp.utils;

import com.example.parkeeriotapp.model.SlotParkir;

import io.realm.Realm;

public class RealmSeeder {

    public static void seedSlotData(Realm realm, int mallId) {
        realm.executeTransaction(r -> {
            for (int i = 1; i <= 8; i++) {
                SlotParkir slot = r.createObject(SlotParkir.class, mallId + "-A-" + i);
                slot.setSlotName("A-" + String.format("%02d", i));
                slot.setMallId(mallId);
                slot.setBooked(false);
            }
        });
    }
}

