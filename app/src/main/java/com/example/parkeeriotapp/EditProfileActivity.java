package com.example.parkeeriotapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullname, edtPhone, edtEmail;
    private Button btnSaveProfile;
    private ImageView imvLeftArrow;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // === Init Firebase ===
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // === Inisialisasi View ===
        edtFullname = findViewById(R.id.edtFullname);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        imvLeftArrow = findViewById(R.id.imvLeftArrow);

        // Tombol kembali
        imvLeftArrow.setOnClickListener(v -> finish());

        // Cek autentikasi user
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ambil UID user aktif
        String uid = auth.getCurrentUser().getUid();
        userRef = db.collection("users").document(uid);

        // Tampilkan data user
        loadProfile();

        // Tombol Simpan
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        edtFullname.setText(snapshot.getString("fullname"));
                        edtPhone.setText(snapshot.getString("phone"));
                        edtEmail.setText(snapshot.getString("email"));

                        // Email tidak dapat diubah
                        edtEmail.setEnabled(false);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal memuat profil: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveProfile() {
        String newFullname = edtFullname.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();

        if (newFullname.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Nama dan nomor HP wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPhone.length() < 8) {
            Toast.makeText(this, "Nomor HP terlalu pendek", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullname", newFullname);
        updates.put("phone", newPhone);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui ✅", Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setText("SAVE");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("SAVE");
                });
    }
}