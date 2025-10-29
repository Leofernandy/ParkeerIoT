package com.example.parkeeriotapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    private static final String PREF_NAME = "ParkeerPrefs";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_NAME = "userName";
    private static final String KEY_PHONE = "userPhone";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 🔹 Simpan data user (kalau mau simpan setelah login)
    public void saveUser(String email, String name, String phone) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    // 🔹 Ambil data user
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFullname() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    // 🔹 Hapus data user saat logout
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}