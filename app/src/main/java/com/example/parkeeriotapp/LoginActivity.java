package com.example.parkeeriotapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txvLinkSignUp, txvForgotPass;
    private CheckBox cbxKeepMe;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // === Inisialisasi View ===
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txvLinkSignUp = findViewById(R.id.txvLinkSignUp);
        txvForgotPass = findViewById(R.id.txvForgotPass);
        cbxKeepMe = findViewById(R.id.cbxKeepMe);

        // === Firebase ===
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // === Auto-login jika sudah login dan pilih “Keep me logged in” ===
        if (auth.getCurrentUser() != null && cbxKeepMe.isChecked()) {
            goToHome();
        }

        // === Tombol LOGIN ===
        btnLogin.setOnClickListener(v -> loginUser());

        // === Tombol Sign Up ===
        txvLinkSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        // === Forgot Password ===
        txvForgotPass.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Masukkan email untuk reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Link reset dikirim ke email", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal kirim link reset: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Processing...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    db.collection("users").document(auth.getCurrentUser().getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                                    goToHome();
                                } else {
                                    Toast.makeText(this, "Data user tidak ditemukan di Firestore", Toast.LENGTH_SHORT).show();
                                }
                                btnLogin.setEnabled(true);
                                btnLogin.setText("LOGIN");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gagal ambil data user", Toast.LENGTH_SHORT).show();
                                btnLogin.setEnabled(true);
                                btnLogin.setText("LOGIN");
                            });
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");

                    String msg = e.getMessage();
                    if (msg.contains("password")) msg = "Password salah!";
                    else if (msg.contains("no user")) msg = "Akun tidak ditemukan!";
                    else if (msg.contains("badly formatted")) msg = "Format email tidak valid!";
                    Toast.makeText(this, "Login gagal: " + msg, Toast.LENGTH_SHORT).show();
                });
    }

    private void goToHome() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}