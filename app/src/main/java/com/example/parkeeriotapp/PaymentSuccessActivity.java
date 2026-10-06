package com.example.parkeeriotapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaymentSuccessActivity extends AppCompatActivity {

    private boolean isWebOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_success);

        String bId = getIntent().getStringExtra("bookingId");
        String invoiceUrl = getIntent().getStringExtra("invoiceUrl");

        // 1. Langsung buka WebView internal kita
        if (invoiceUrl != null && !isWebOpened) {
            Intent webIntent = new Intent(this, PaymentWebActivity.class);
            webIntent.putExtra("invoiceUrl", invoiceUrl);

            webIntent.putExtra("bookingId", bId);

            startActivity(webIntent);
            isWebOpened = true;
        }

        // 2. Cek status pembayaran secara real-time dari Firebase
        if (bId != null) {
            DatabaseReference bRef = FirebaseDatabase.getInstance().getReference("bookings").child(bId);
            bRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status = snapshot.child("status").getValue(String.class);

                    // 🚀 TARIK DATA plateScanned DARI FIREBASE
                    Boolean plateScanned = snapshot.child("plateScanned").getValue(Boolean.class);

                    if ("booked".equals(status)) {
                        // Kalau AI CCTV udah nge-scan platnya (plateScanned = true)
                        if (plateScanned != null && plateScanned) {
                            Toast.makeText(PaymentSuccessActivity.this, "✅ Akses Diterima! Plat nomor terverifikasi.", Toast.LENGTH_LONG).show();
                        }
                        // Kalau baru aja selesai bayar (plateScanned masih false/null)
                        else {
                            Toast.makeText(PaymentSuccessActivity.this, "💳 Pembayaran Berhasil! Silakan menuju lokasi parkir.", Toast.LENGTH_LONG).show();
                        }
                    }
                    // ---> LOGIKA EXPIRED <---
                    else if ("expired".equals(status)) {
                        Toast.makeText(PaymentSuccessActivity.this, "⏳ Waktu Habis! Booking Dibatalkan otomatis.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // 3. Tombol lihat detail
        Button btnBookDetail = findViewById(R.id.btnBookDetail);
        btnBookDetail.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentSuccessActivity.this, BookDetailsActivity.class);
            intent.putExtra("bookingId", bId);
            intent.putExtra("mallName", getIntent().getStringExtra("mallName"));
            intent.putExtra("mallAddress", getIntent().getStringExtra("mallAddress"));
            intent.putExtra("slot", getIntent().getStringExtra("slot"));
            intent.putExtra("plate", getIntent().getStringExtra("plate"));
            intent.putExtra("jamMasuk", getIntent().getStringExtra("jamMasuk"));
            intent.putExtra("jamKeluar", getIntent().getStringExtra("jamKeluar"));
            intent.putExtra("totalHarga", getIntent().getIntExtra("totalHarga", 0));
            intent.putExtra("durasiMenit", getIntent().getLongExtra("durasiMenit", 0));
            startActivity(intent);
            finish();
        });
    }
}