package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_success);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnBookDetail = findViewById(R.id.btnBookDetail);
        btnBookDetail.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentSuccessActivity.this, BookDetailsActivity.class);
            intent.putExtra("bookingId", getIntent().getStringExtra("bookingId"));
            intent.putExtra("mallName", getIntent().getStringExtra("mallName"));
            intent.putExtra("mallAddress", getIntent().getStringExtra("mallAddress"));
            intent.putExtra("slot", getIntent().getStringExtra("slot"));
            intent.putExtra("plate", getIntent().getStringExtra("plate"));
            intent.putExtra("jamMasuk", getIntent().getStringExtra("jamMasuk"));
            intent.putExtra("jamKeluar", getIntent().getStringExtra("jamKeluar"));
            intent.putExtra("totalHarga", getIntent().getIntExtra("totalHarga", 0));
            intent.putExtra("durasiMenit", getIntent().getLongExtra("durasiMenit", 0));
            startActivity(intent);
        });
    }

    public void toBookingDetails(){
        Intent intent = new Intent(this, BookDetailsActivity.class);
        startActivity(intent);
    }
}