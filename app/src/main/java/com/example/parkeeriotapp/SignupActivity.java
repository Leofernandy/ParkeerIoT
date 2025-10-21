package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.parkeeriotapp.model.User;

import io.realm.Realm;

public class SignupActivity extends AppCompatActivity {

    EditText edtFullname, edtEmail, edtPhone, edtPassword;
    TextView txvLinkLogin;
    ImageView imvLeftArrow;
    Button btnSignup;

    CheckBox cbxTnC;
    Realm realm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        realm = Realm.getDefaultInstance();


        edtFullname = findViewById(R.id.edtFullname);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignup = findViewById(R.id.btnSignup);
        cbxTnC = findViewById(R.id.cbxTnC);


        btnSignup.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String fullname = edtFullname.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (fullname.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.matches("[0-9]{10,15}")) {
                Toast.makeText(this, "Nomor HP harus 10–15 digit angka", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbxTnC.isChecked()) {
                Toast.makeText(this, "Harap setujui syarat dan ketentuan terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            User existing = realm.where(User.class).equalTo("email", email).findFirst();
            if (existing != null) {
                Toast.makeText(this, "Email sudah digunakan", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                realm.executeTransaction(r -> {
                    User user = r.createObject(User.class, email);
                    user.setFullname(fullname);
                    user.setPhone(phone);
                    user.setPassword(password);
                });

                Toast.makeText(this, "Signup berhasil", Toast.LENGTH_SHORT).show();

                new android.os.Handler().postDelayed(() -> {
                    finish(); // balik ke LoginActivity
                }, 1000);

            } catch (Exception e) {
                Toast.makeText(this, "Gagal signup: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imvLeftArrow = findViewById(R.id.imvLeftArrow);
        imvLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        txvLinkLogin = findViewById(R.id.txvLinkLogin);

        txvLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLogin();
            }
        });
    }

    public void toLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

}