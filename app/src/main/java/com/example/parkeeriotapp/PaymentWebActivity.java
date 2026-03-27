package com.example.parkeeriotapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaymentWebActivity extends AppCompatActivity {

    private WebView webView;
    private TextView tvTimer;
    private CountDownTimer expirationTimer;
    private String bookingId;
    private String mallId;
    private String slotId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_web);

        webView = findViewById(R.id.webView);
        tvTimer = findViewById(R.id.tvTimer);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String invoiceUrl = getIntent().getStringExtra("invoiceUrl");
        bookingId = getIntent().getStringExtra("bookingId");

        // --- MONITORING STATUS ---
        if (bookingId != null) {
            // Jika ID dimulai dengan BK (Booking), ambil info slot untuk timer
            if (bookingId.startsWith("BK")) {
                DatabaseReference bRef = FirebaseDatabase.getInstance().getReference("bookings").child(bookingId);
                bRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            mallId = snapshot.child("mallId").getValue(String.class);
                            slotId = snapshot.child("slot").getValue(String.class);
                            startExpirationTimer();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

                // Pantau status lunas/expired untuk Booking
                bRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.child("status").getValue(String.class);
                        if ("booked".equals(status) || "expired".equals(status)) {
                            if (expirationTimer != null) expirationTimer.cancel();
                            if ("expired".equals(status)) {
                                Toast.makeText(PaymentWebActivity.this, "Waktu Habis! Booking Dibatalkan.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            // Jika ID dimulai dengan TU (Top Up), kita bisa pantau lewat RTDB topups
            else if (bookingId.startsWith("TU")) {
                DatabaseReference tRef = FirebaseDatabase.getInstance().getReference("topups").child(bookingId);
                tRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.child("status").getValue(String.class);
                            if ("SUCCESS".equals(status)) {
                                Toast.makeText(PaymentWebActivity.this, "Top Up Berhasil!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("parkeerapp://success")) {
                    if (expirationTimer != null) expirationTimer.cancel();
                    finish();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        if (invoiceUrl != null) {
            webView.loadUrl(invoiceUrl);
        }
    }

    private void startExpirationTimer() {
        expirationTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long detikTersisa = millisUntilFinished / 1000;
                tvTimer.setText("Sisa Waktu Bayar: " + detikTersisa + " detik");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("WAKTU HABIS!");
                // HANYA update slot jika ini adalah transaksi BOOKING (BK)
                if (bookingId != null && bookingId.startsWith("BK") && mallId != null && slotId != null) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    dbRef.child("bookings").child(bookingId).child("status").setValue("expired");
                    dbRef.child("bookings").child(bookingId).child("payment_status").setValue("EXPIRED");
                    dbRef.child("slots").child(mallId).child(slotId).child("status").setValue("available");
                    dbRef.child("slots").child(mallId).child(slotId).child("bookingId").setValue("");
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (expirationTimer != null) expirationTimer.cancel();
    }
}