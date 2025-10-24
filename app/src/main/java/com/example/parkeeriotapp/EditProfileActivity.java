package com.example.parkeeriotapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkeeriotapp.utils.UserSessionManager;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullname, edtPhone, edtEmail;
    private Button btnSaveProfile;
    private ImageView imvLeftArrow;
    private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        session = new UserSessionManager(this);

        edtFullname = findViewById(R.id.edtFullname);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        imvLeftArrow = findViewById(R.id.imvLeftArrow);

        // Isi field dari session
        if (session.isLoggedIn()) {
            edtFullname.setText(session.getFullname());
            edtPhone.setText(session.getPhone());
            edtEmail.setText(session.getEmail());
        }

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        imvLeftArrow.setOnClickListener(v -> finish());
    }

    private void saveProfile() {
        String newFullname = edtFullname.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim(); // Email sebagai ID

        if (newFullname.isEmpty() || newPhone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }



        Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
        finish();
    }
}
