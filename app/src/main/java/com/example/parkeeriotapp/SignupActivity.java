package com.example.parkeeriotapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText edtFullname, edtEmail, edtPhone, edtPassword;
    private CheckBox cbxTnC;
    private Button btnSignup;
    private TextView txvLinkLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // === Inisialisasi Firebase ===
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // === Inisialisasi View ===
        edtFullname = findViewById(R.id.edtFullname);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        cbxTnC = findViewById(R.id.cbxTnC);
        btnSignup = findViewById(R.id.btnSignup);
        txvLinkLogin = findViewById(R.id.txvLinkLogin);

        // === Link ke Login ===
        txvLinkLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        // === Tombol Signup ===
        btnSignup.setOnClickListener(v -> {
            String fullname = edtFullname.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(fullname) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbxTnC.isChecked()) {
                Toast.makeText(this, "Harap setujui Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(fullname, email, phone, password);
        });
    }

    private void registerUser(String fullname, String email, String phone, String password) {
        btnSignup.setEnabled(false);
        btnSignup.setText("Membuat akun...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnSignup.setEnabled(true);
                        btnSignup.setText("SIGN UP");

                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user.getUid(), fullname, email, phone);
                            }
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "Terjadi kesalahan";
                            Toast.makeText(SignupActivity.this, "Gagal daftar: " + msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String fullname, String email, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullname", fullname);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("saldo", 0);
        userData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignupActivity.this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}