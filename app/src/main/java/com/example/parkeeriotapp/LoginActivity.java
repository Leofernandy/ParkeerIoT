package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.parkeeriotapp.model.SlotParkir;
import com.example.parkeeriotapp.utils.UserSessionManager;

import com.example.parkeeriotapp.model.User;
import com.example.parkeeriotapp.utils.RealmSeeder;

import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txvLinkSignUp;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        realm = Realm.getDefaultInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txvLinkSignUp = findViewById(R.id.txvLinkSignUp);

        btnLogin.setOnClickListener(v -> doLogin());

        txvLinkSignUp.setOnClickListener(v -> toSignUp());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void doLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = realm.where(User.class)
                .equalTo("email", email)
                .equalTo("password", password)
                .findFirst();


        if (user != null) {
            Log.d("DEBUG", "User login: " + user.getEmail());
            UserSessionManager session = new UserSessionManager(this);
            session.createLoginSession(
                    user.getEmail(),
                    user.getFullname(),
                    user.getPhone(),
                    user.getPassword()
            );
            session.syncSaldoFromWallet(this);

            int mallId = 1;
            if (realm.where(SlotParkir.class).equalTo("mallId", mallId).findAll().isEmpty()) {
                RealmSeeder.seedSlotData(realm, mallId);
            }

            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show();
            Log.d("DEBUG", "Login gagal: user tidak ditemukan di Realm");
        }
    }
        private void toSignUp() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
