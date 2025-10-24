package com.example.parkeeriotapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class UserSessionManager {

    private static final String PREF_NAME = "ParkeerPrefs";
    private static final String KEY_EMAILS = "registeredEmails";
    private static final String KEY_FULLNAME_PREFIX = "fullname_";
    private static final String KEY_PHONE_PREFIX = "phone_";
    private static final String KEY_PASSWORD_PREFIX = "password_";
    private static final String KEY_LOGGED_IN_EMAIL = "loggedInEmail";
    private static final String KEY_SALDO_PREFIX = "saldo_";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ======= Registrasi =======
    public boolean isEmailRegistered(String email) {
        Set<String> emails = prefs.getStringSet(KEY_EMAILS, new HashSet<>());
        return emails.contains(email);
    }

    public void registerUser(String email, String fullname, String phone, String password) {
        Set<String> emails = new HashSet<>(prefs.getStringSet(KEY_EMAILS, new HashSet<>()));
        emails.add(email);
        editor.putStringSet(KEY_EMAILS, emails);
        editor.putString(KEY_FULLNAME_PREFIX + email, fullname);
        editor.putString(KEY_PHONE_PREFIX + email, phone);
        editor.putString(KEY_PASSWORD_PREFIX + email, password);
        editor.putInt(KEY_SALDO_PREFIX + email, 0); // saldo awal 0
        editor.apply();
    }

    // ======= Login / Logout =======
    public boolean login(String email, String password) {
        if (!isEmailRegistered(email)) return false;
        String storedPassword = prefs.getString(KEY_PASSWORD_PREFIX + email, null);
        if (storedPassword != null && storedPassword.equals(password)) {
            editor.putString(KEY_LOGGED_IN_EMAIL, email);
            editor.apply();
            return true;
        }
        return false;
    }

    public void logout() {
        editor.remove(KEY_LOGGED_IN_EMAIL);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_LOGGED_IN_EMAIL);
    }

    public String getLoggedInEmail() {
        return prefs.getString(KEY_LOGGED_IN_EMAIL, null);
    }

    // ======= Ambil info user =======
    public String getFullname() {
        String email = getLoggedInEmail();
        if (email == null) return null;
        return prefs.getString(KEY_FULLNAME_PREFIX + email, null);
    }

    public String getPhone() {
        String email = getLoggedInEmail();
        if (email == null) return null;
        return prefs.getString(KEY_PHONE_PREFIX + email, null);
    }

    public String getEmail() {
        return getLoggedInEmail();
    }

    public int getSaldo() {
        String email = getLoggedInEmail();
        if (email == null) return 0;
        return prefs.getInt(KEY_SALDO_PREFIX + email, 0);
    }

    public void setSaldo(int saldo) {
        String email = getLoggedInEmail();
        if (email != null) {
            editor.putInt(KEY_SALDO_PREFIX + email, saldo);
            editor.apply();
        }
    }

    // ======= Update profile =======
    public void updateProfile(String fullname, String phone) {
        String email = getLoggedInEmail();
        if (email != null) {
            editor.putString(KEY_FULLNAME_PREFIX + email, fullname);
            editor.putString(KEY_PHONE_PREFIX + email, phone);
            editor.apply();
        }
    }

    // ======= Dapatkan password user (opsional) =======
    public String getPassword() {
        String email = getLoggedInEmail();
        if (email == null) return null;
        return prefs.getString(KEY_PASSWORD_PREFIX + email, null);
    }

    // ======= Sync saldo (dummy, bisa dikembangkan) =======
    public void syncSaldoFromWallet(Context context) {
        // kalau ada logika topup/saldo dari server, bisa di sini
        // untuk sekarang cukup pakai saldo di SharedPreferences
    }
}
