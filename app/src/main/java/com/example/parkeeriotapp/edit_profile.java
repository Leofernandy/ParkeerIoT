package com.example.parkeeriotapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.parkeeriotapp.model.User;
import com.example.parkeeriotapp.utils.UserSessionManager;

import io.realm.Realm;

public class edit_profile extends AppCompatActivity {

    private EditText edtFullname, edtPhone, edtEmail;
    private Button btnSaveProfile;
    private ImageView imvLeftArrow;
    private Realm realm;
    private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Realm.init(this);
        realm = Realm.getDefaultInstance();
        session = new UserSessionManager(this);

        edtFullname = findViewById(R.id.edtFullname);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        imvLeftArrow = findViewById(R.id.imvLeftArrow);

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

        User user = realm.where(User.class)
                .equalTo("email", email)
                .findFirst();

        if (user != null) {
            realm.executeTransaction(r -> {
                user.setFullname(newFullname);
                user.setPhone(newPhone);
            });

            // Update SharedPreferences juga
            session.createLoginSession(
                    user.getEmail(),
                    user.getFullname(),
                    user.getPhone(),
                    user.getPassword() // tetap gunakan password yang lama
            );

            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
